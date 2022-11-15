package com.skydo.lib.fsm.listener;

import com.skydo.lib.fsm.config.StateValidatorConfig;
import com.skydo.lib.fsm.internal.synchronization.FSMProcess;
import com.skydo.lib.fsm.internal.synchronization.work.FSMTransitionWork;
import com.skydo.lib.fsm.internal.synchronization.work.FSMWorkUnit;
import com.skydo.lib.fsm.servicecontributor.FSMService;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.*;

public class FSMPostUpdateListener extends BaseEventListener implements PostUpdateEventListener {

	private final Logger log = LoggerFactory.getLogger(FSMPostUpdateListener.class.getSimpleName());

	public FSMPostUpdateListener(FSMService fsmService) {
		super(fsmService);
	}

	private void onPostUpdateValidatorCheck(PostUpdateEvent postUpdateEvent) {

		StateValidatorConfig stateValidatorConfig = getFsmService().getStateValidator();
		HashMap<Class<?>, HashMap<String, HashMap<String, Method>>> validatorMap = stateValidatorConfig.getEntityFieldValidatorMap();
		Object entity = postUpdateEvent.getEntity();
		final FSMProcess fsmProcess = getFsmService().getFsmProcessManager().get(postUpdateEvent.getSession());
		Object[] entityOldState = fsmProcess.getCachedEntityState(
			postUpdateEvent.getId(), postUpdateEvent.getPersister().getEntityName()
		);

		Class<? extends Object> entityClass = entity.getClass();
		HashMap<String, HashMap<String, Method>> fieldToValuesMap = validatorMap.get(entityClass);

		if (validatorMap.containsKey(entityClass)) {
			String[] propertyNames = postUpdateEvent.getPersister().getPropertyNames();
			int[] dirtyProperties = postUpdateEvent.getDirtyProperties();

			for (int propertyIndex: dirtyProperties) {
				String propertyName = propertyNames[propertyIndex];

				if (fieldToValuesMap.containsKey(propertyName)) {
					String newValue = postUpdateEvent.getState()[propertyIndex].toString();
					String oldValue = entityOldState[propertyIndex].toString();
					HashMap<String, Method> valuesToValidatorMethods = fieldToValuesMap.get(propertyName);

					if (valuesToValidatorMethods.containsKey(newValue)) {
						Method validatorMethod = valuesToValidatorMethods.get(newValue);
						Class<?> declaringClass = validatorMethod.getDeclaringClass();
						try {
							validatorMethod.invoke(
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
	}

	private void onPostUpdateTransitionCheck(PostUpdateEvent event) {

		final String entityName = event.getPersister().getEntityName();

		if (getFsmService().getEntitiesConfigurations().isStateManaged(entityName)) {
			checkIfTransactionInProgress(event.getSession());

			final FSMProcess fsmProcess = getFsmService().getFsmProcessManager().get(event.getSession());

			Object[] oldState = getOldDBState(fsmProcess, entityName, event);

			FSMWorkUnit workUnit = new FSMTransitionWork(
				event.getSession(),
				entityName,
				getFsmService(),
				event.getId(),
				event.getPersister(),
				oldState,
				event.getState()
			);

			workUnit.perform();

		}
	}

	private Object[] getOldDBState(FSMProcess fsmProcess, String entityName, PostUpdateEvent event) {
		return event.getOldState();
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

	@Override
	public void onPostUpdate(PostUpdateEvent postUpdateEvent) {
		onPostUpdateTransitionCheck(postUpdateEvent);
		onPostUpdateValidatorCheck(postUpdateEvent);
		onPostUpdateActionExecutor(postUpdateEvent);
	}

	@Override
	public boolean requiresPostCommitHanding(EntityPersister entityPersister) {
		return false;
	}
}



















