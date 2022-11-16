package com.skydo.lib.fsm.listener;

import com.skydo.lib.fsm.config.StateValidatorConfig;
import com.skydo.lib.fsm.internal.synchronization.FSMProcess;
import com.skydo.lib.fsm.servicecontributor.FSMService;
import org.hibernate.event.spi.PostCommitUpdateEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author Raj Sheth
 */

/**
 * <a href="https://docs.jboss.org/hibernate/stable/orm/javadocs/org/hibernate/event/spi/PostCommitUpdateEventListener.html">Doc</a>
 */
public class FSMPostCommitUpdateListener extends BaseEventListener implements PostCommitUpdateEventListener {

	private final static Logger log = LoggerFactory.getLogger(FSMPostCommitUpdateListener.class);

	public FSMPostCommitUpdateListener(FSMService fsmService) {
		super(fsmService);
	}

	private boolean onPostUpdateActionExecutor(PostUpdateEvent postUpdateEvent) {
		StateValidatorConfig stateValidatorConfig = getFsmService().getStateValidator();
		HashMap<Class<?>, HashMap<String, HashMap<String, Method>>> entityFieldPostUpdateActionMap
				= stateValidatorConfig.getEntityFieldPostUpdateActionMap();

		Object entity = postUpdateEvent.getEntity();

		Class<? extends Object> entityClass = entity.getClass();
		HashMap<String, HashMap<String, Method>> fieldToValuesMap = entityFieldPostUpdateActionMap.get(entityClass);

		if (entityFieldPostUpdateActionMap.containsKey(entityClass)) {
			String[] propertyNames = postUpdateEvent.getPersister().getPropertyNames();
			List oldValues = Arrays.stream(postUpdateEvent.getOldState()).toList();
			List newValues = Arrays.stream(postUpdateEvent.getState()).toList();
			int totalFields = oldValues.size();

			for(int i = 0 ; i < totalFields ; ++i) {
				String propertyName = propertyNames[i];
				Object currentOldValue = oldValues.get(i).toString();
				Object currentNewValue = newValues.get(i).toString();
				log.info("Property: " + propertyName + " currentOldValue: " + currentOldValue + " currentNewValue" + currentNewValue);
				if (!currentOldValue.equals(currentNewValue)) {
					log.info("DIFFERENT values");
					if (fieldToValuesMap.containsKey(propertyName)) {
						log.info("Yes:: `fieldToValuesMap` contains the property:");
						HashMap<String, Method> valuesToPostActionMethods = fieldToValuesMap.get(propertyName);

						if (valuesToPostActionMethods.containsKey(currentNewValue)) {
							log.info("Yes:: `valuesToPostActionMethods` contains the currentNewValue: " + currentNewValue);
							Method postActionMethod = valuesToPostActionMethods.get(currentNewValue);
							Class<?> declaringClass = postActionMethod.getDeclaringClass();
							try {
								postActionMethod.invoke(
									// TODO: Is this assumption correct? Accessing constructor at zeroth index?
									declaringClass.getConstructors()[0].newInstance(),
									postUpdateEvent.getId(),
									currentOldValue,
									currentNewValue
								);
							} catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
								throw new RuntimeException(e);
							}
						}
					}
				}
			}
		}
		return true;
	}


	/**
	 * Called when a commit fails and an entity was scheduled for update
	 *
	 * @param event the update event to be handled
	 */
	@Override
	public void onPostUpdateCommitFailed(PostUpdateEvent event) {}

	@Override
	public void onPostUpdate(PostUpdateEvent event) {
		onPostUpdateActionExecutor(event);
	}

	/**
	 * Does this listener require that after transaction hooks be registered?
	 *
	 * @param persister The persister for the entity in question.
	 * @return {@code true} if after transaction callbacks should be added.
	 * @deprecated use {@link #requiresPostCommitHandling(EntityPersister)}
	 */
	@Override
	public boolean requiresPostCommitHanding(EntityPersister persister) {
		return true;
	}
}
