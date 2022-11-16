package com.skydo.lib.fsm.listener;

import com.skydo.lib.fsm.config.StateValidatorConfig;
import com.skydo.lib.fsm.internal.synchronization.FSMProcess;
import com.skydo.lib.fsm.servicecontributor.FSMService;
import com.skydo.lib.fsm.servicecontributor.FSMServiceImpl;
import org.hibernate.event.spi.PostCommitUpdateEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

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
		final FSMProcess fsmProcess = getFsmService().getFsmProcessManager().get(postUpdateEvent.getSession());
		Object[] entityOldState = fsmProcess.getCachedEntityState(
				postUpdateEvent.getId(), postUpdateEvent.getPersister().getEntityName()
		);

		Class<? extends Object> entityClass = entity.getClass();
		HashMap<String, HashMap<String, Method>> fieldToValuesMap = entityFieldPostUpdateActionMap.get(entityClass);

		if (entityFieldPostUpdateActionMap.containsKey(entityClass)) {
			String[] propertyNames = postUpdateEvent.getPersister().getPropertyNames();
			int[] dirtyProperties = postUpdateEvent.getDirtyProperties();

			for (int propertyIndex: dirtyProperties) {
				String propertyName = propertyNames[propertyIndex];

				if (fieldToValuesMap.containsKey(propertyName)) {
					String newValue = postUpdateEvent.getState()[propertyIndex].toString();
					String oldValue = entityOldState[propertyIndex].toString();
					HashMap<String, Method> valuesToPostActionMethods = fieldToValuesMap.get(propertyName);

					if (valuesToPostActionMethods.containsKey(newValue)) {
						Method postActionMethod = valuesToPostActionMethods.get(newValue);
						Class<?> declaringClass = postActionMethod.getDeclaringClass();
						try {
							postActionMethod.invoke(
									declaringClass.getConstructors()[0].newInstance(),
									postUpdateEvent.getId(),
									oldValue,
									newValue
							);
						} catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
							throw new RuntimeException(e);
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
	public void onPostUpdateCommitFailed(PostUpdateEvent event) {
		log.info("onPostUpdateCommitFailed here 1" + event);
		log.info("onPostUpdateCommitFailed here 2" + event);
	}

	@Override
	public void onPostUpdate(PostUpdateEvent event) {
		log.info("on Post Update here 1 " + event);
		log.info("on Post Update here 2 " + event);
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
		log.info("Inside requiresPostCommitHanding---- " + persister);
		return true;
	}
}
