package com.skydo.lib.fsm.listener;

import com.skydo.lib.fsm.config.StateValidatorConfig;
import com.skydo.lib.fsm.internal.synchronization.FSMProcess;
import com.skydo.lib.fsm.internal.tools.Pair;
import com.skydo.lib.fsm.servicecontributor.FSMService;
import org.hibernate.FlushMode;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

public class FSMPreUpdateListener extends BaseEventListener implements PreUpdateEventListener {

    private final Logger log = LoggerFactory.getLogger(FSMPreUpdateListener.class.getSimpleName());
    public FSMPreUpdateListener(FSMService fsmService) {
        super(fsmService);
    }

    private void onPostUpdateValidatorCheck(PreUpdateEvent postUpdateEvent) {
        FlushMode originalFlushMode = postUpdateEvent.getSession().getHibernateFlushMode();
        postUpdateEvent.getSession().setHibernateFlushMode(FlushMode.COMMIT);
        StateValidatorConfig stateValidatorConfig = getFsmService().getStateValidator();
        HashMap<Class<?>, HashMap<String, HashMap<String, Pair<Object, List<Method>>>>> validatorMap = stateValidatorConfig.getValidatorMap();
        Object entity = postUpdateEvent.getEntity();
        final FSMProcess fsmProcess = getFsmService().getFsmProcessManager().get(postUpdateEvent.getSession());
        Class<? extends Object> entityClass = entity.getClass();
        HashMap<String, HashMap<String, Pair<Object, List<Method>>>> fieldToValuesMap = validatorMap.get(entityClass);

        if (validatorMap.containsKey(entityClass)) {
            String[] propertyNames = postUpdateEvent.getPersister().getPropertyNames();
            for (int propertyIndex = 0; propertyIndex < propertyNames.length; propertyIndex++) {
                String propertyName = propertyNames[propertyIndex];
                if (fieldToValuesMap.containsKey(propertyName)) {
                    String newValue = postUpdateEvent.getState()[propertyIndex].toString();
                    String oldValue = postUpdateEvent.getOldState()[propertyIndex].toString();
                    if (newValue != null && !newValue.equals(oldValue)) {
                        HashMap<String, Pair<Object, List<Method>>> valuesToValidators = fieldToValuesMap.get(propertyName);
                        Pair<Object, List<Method>> validator = valuesToValidators.get(newValue);
                        if (validator != null && validator.getSecond() != null) {
                            validator.getSecond().forEach(method -> {
                                try {
                                    method.invoke(validator.getFirst(), postUpdateEvent.getId(), oldValue, newValue);
                                } catch (IllegalAccessException | InvocationTargetException e) {
                                    log.error("Something went wrong invoking the validator method::: " + e.getCause());
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    }
                }
            }
        }
        postUpdateEvent.getSession().setHibernateFlushMode(originalFlushMode);
    }

    private Object[] getOldDBState(FSMProcess fsmProcess, String entityName, PreUpdateEvent event) {
        return event.getOldState();
    }

    @Override
    public boolean onPreUpdate(PreUpdateEvent event) {
        final String entityName = event.getPersister().getEntityName();

        if (getFsmService().getEntitiesConfigurations().isStateManaged(entityName)) {
            checkIfTransactionInProgress(event.getSession());

            Object[] currentState = event.getPersister().getDatabaseSnapshot(event.getId(), event.getSession());

            final FSMProcess fsmProcess = getFsmService().getFsmProcessManager().get(event.getSession());
//            fsmProcess.cacheEntityState(event.getId(), entityName, currentState);
            fsmProcess.cacheEvent(event);
        }
//        onPostUpdateValidatorCheck(event);

        return false;
    }
}
