package com.skydo.lib.fsm.listener;

import com.skydo.lib.fsm.config.StateValidatorConfig;
import com.skydo.lib.fsm.internal.synchronization.FSMProcess;
import com.skydo.lib.fsm.internal.synchronization.work.FSMTransitionWork;
import com.skydo.lib.fsm.internal.synchronization.work.FSMWorkUnit;
import com.skydo.lib.fsm.internal.tools.Pair;
import com.skydo.lib.fsm.servicecontributor.FSMService;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

public class FSMPostUpdateListener extends BaseEventListener implements PostUpdateEventListener {

    private final Logger log = LoggerFactory.getLogger(FSMPostUpdateListener.class.getSimpleName());

    public FSMPostUpdateListener(FSMService fsmService) {
        super(fsmService);
    }

    private void onPostUpdateValidatorCheck(PostUpdateEvent postUpdateEvent) {

        StateValidatorConfig stateValidatorConfig = getFsmService().getStateValidator();
        HashMap<Class<?>, HashMap<String, HashMap<String, Pair<Object, List<Method>>>>> validatorMap = stateValidatorConfig.getValidatorMap();
        Object entity = postUpdateEvent.getEntity();
        final FSMProcess fsmProcess = getFsmService().getFsmProcessManager().get(postUpdateEvent.getSession());
        Object[] entityOldState = fsmProcess.getCachedEntityState(postUpdateEvent.getId(), postUpdateEvent.getPersister().getEntityName());
        Class<? extends Object> entityClass = entity.getClass();
        HashMap<String, HashMap<String, Pair<Object, List<Method>>>> fieldToValuesMap = validatorMap.get(entityClass);

        if (validatorMap.containsKey(entityClass)) {
            String[] propertyNames = postUpdateEvent.getPersister().getPropertyNames();
            int[] dirtyProperties = postUpdateEvent.getDirtyProperties();
            for (int propertyIndex : dirtyProperties) {
                String propertyName = propertyNames[propertyIndex];
                if (fieldToValuesMap.containsKey(propertyName)) {
                    String newValue = postUpdateEvent.getState()[propertyIndex].toString();
                    String oldValue = entityOldState[propertyIndex].toString();
                    HashMap<String, Pair<Object, List<Method>>> valuesToValidators = fieldToValuesMap.get(propertyName);
                    Pair<Object, List<Method>> validator = valuesToValidators.get(newValue);
                    if (validator != null && validator.getSecond() != null) {
                        validator.getSecond().forEach(method -> {
                            try {
                                method.invoke(validator.getFirst(), postUpdateEvent.getId(), oldValue, newValue);
                            } catch (IllegalAccessException | InvocationTargetException  e) {
                                log.error("Something went wrong invoking the validator method::: " + e.getCause());
                                throw new RuntimeException(e);
                            }
                        });
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

    @Override
    public void onPostUpdate(PostUpdateEvent postUpdateEvent) {

        onPostUpdateTransitionCheck(postUpdateEvent);

        onPostUpdateValidatorCheck(postUpdateEvent);
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister entityPersister) {
        return false;
    }
}



















