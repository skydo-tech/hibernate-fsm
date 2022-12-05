package com.skydo.lib.fsm.listener;

import com.skydo.lib.fsm.config.StateValidatorConfig;
import com.skydo.lib.fsm.internal.synchronization.FSMProcess;
import com.skydo.lib.fsm.internal.tools.Pair;
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
		HashMap<Class<?>, HashMap<String, HashMap<String, Pair<Object, List<Method>>>>> entityFieldPostUpdateActionMap
			= stateValidatorConfig.getEntityFieldPostUpdateActionMap();

		Object entity = postUpdateEvent.getEntity();

		Class<? extends Object> entityClass = entity.getClass();
		HashMap<String, HashMap<String, Pair<Object, List<Method>>>> fieldToValuesMap = entityFieldPostUpdateActionMap.get(entityClass);

		if (entityFieldPostUpdateActionMap.containsKey(entityClass)) {
			String[] propertyNames = postUpdateEvent.getPersister().getPropertyNames();
			List oldValues = Arrays.stream(postUpdateEvent.getOldState()).toList();
			List newValues = Arrays.stream(postUpdateEvent.getState()).toList();
			int totalFields = oldValues.size();

			for(int i = 0 ; i < totalFields ; ++i) {
				String propertyName = propertyNames[i];
				Object currentOldValue = null;
				if (oldValues.get(i) != null) {
					currentOldValue = oldValues.get(i).toString();
				}
				Object currentNewValue = null;
				if (newValues.get(i) != null) {
					currentNewValue = newValues.get(i).toString();
				}
				if (currentOldValue == null && currentNewValue == null) {
					continue;
				}
				// oldValue -   newValue  -->     skip/execute `postUpdateAction`
				// null     -   null      -->     skip
				// null     -   ABC       -->     execute
				// ABC      -   null      -->     skip
				// ABC      -   CBA       -->     execute
				if (
					currentOldValue != null && !currentNewValue.equals(currentOldValue)
				) {
					if (fieldToValuesMap.containsKey(propertyName)) {
						HashMap<String, Pair<Object, List<Method>>> valuesToPostActionMethods = fieldToValuesMap.get(propertyName);

						if (valuesToPostActionMethods.containsKey(currentNewValue)) {
							Pair<Object, List<Method>> postActionMethodPair = valuesToPostActionMethods.get(currentNewValue);
							if (postActionMethodPair != null && postActionMethodPair.getSecond() != null) {
								Object finalCurrentNewValue = currentNewValue;
								Object finalCurrentOldValue = currentOldValue;
								postActionMethodPair.getSecond().forEach(method -> {
									try {
										method.invoke(
											postActionMethodPair.getFirst(),
											postUpdateEvent.getId(),
											finalCurrentOldValue,
											finalCurrentNewValue
										);
									} catch (IllegalAccessException | InvocationTargetException e) {
										log.error(
											"Something went wrong invoking the post commit action::: " + e.getCause()
										);
										throw new RuntimeException(e);
									}
								});
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
