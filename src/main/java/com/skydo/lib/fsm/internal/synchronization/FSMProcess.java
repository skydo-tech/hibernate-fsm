package com.skydo.lib.fsm.internal.synchronization;

import com.skydo.lib.fsm.config.StateValidatorConfig;
import com.skydo.lib.fsm.internal.tools.Pair;
import com.skydo.lib.fsm.listener.FSMPreUpdateListener;
import com.skydo.lib.fsm.servicecontributor.FSMService;
import org.hibernate.action.spi.BeforeTransactionCompletionProcess;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.spi.PreUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class FSMProcess implements BeforeTransactionCompletionProcess {

    private final SessionImplementor session;

    private final Queue<PreUpdateEvent> preUpdateEventQueue;

    private final FSMService fsmService;

    private final Map<Pair<String, Object>, Object[]> entityStateCache;

    private final Logger log = LoggerFactory.getLogger(FSMPreUpdateListener.class.getSimpleName());

    public FSMProcess(SessionImplementor session, FSMService fsmService) {
        this.session = session;
        this.entityStateCache = new HashMap<>();
        this.preUpdateEventQueue = new LinkedList<PreUpdateEvent>();
        this.fsmService = fsmService;
    }

    public void cacheEntityState(Object id, String entityName, Object[] snapshot) {
        final Pair<String, Object> key = new Pair<>( entityName, id );
        if ( entityStateCache.containsKey( key ) ) {
            throw new RuntimeException( "The entity [" + entityName + "] with id [" + id + "] is already cached." );
        }
        entityStateCache.put( key, snapshot );
    }

    public Object[] getCachedEntityState(Object id, String entityName) {
        final Pair<String, Object> key = new Pair<>( entityName, id );
        final Object[] entityState = entityStateCache.get( key );
        if ( entityState != null ) {
            entityStateCache.remove( key );
        }
        return entityState;
    }

    public void cacheEvent(PreUpdateEvent event){
        preUpdateEventQueue.add(event);
    }

    @Override
    public void doBeforeTransactionCompletion(SessionImplementor session) {
        PreUpdateEvent event;
        while ( ! preUpdateEventQueue.isEmpty() ) {
            event = preUpdateEventQueue.poll();
            onPostUpdateValidatorCheck(event);
       }
    }

    private void onPostUpdateValidatorCheck(PreUpdateEvent postUpdateEvent) {
//        FlushMode originalFlushMode = postUpdateEvent.getSession().getHibernateFlushMode();
//        postUpdateEvent.getSession().setHibernateFlushMode(FlushMode.COMMIT);
        StateValidatorConfig stateValidatorConfig = fsmService.getStateValidator();
        HashMap<Class<?>, HashMap<String, HashMap<String, Pair<Object, List<Method>>>>> validatorMap = stateValidatorConfig.getValidatorMap();
        Object entity = postUpdateEvent.getEntity();
        final FSMProcess fsmProcess = this.fsmService.getFsmProcessManager().get(postUpdateEvent.getSession());
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
//        postUpdateEvent.getSession().setHibernateFlushMode(originalFlushMode);
    }
}
