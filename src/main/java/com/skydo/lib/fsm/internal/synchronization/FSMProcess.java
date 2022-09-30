package com.skydo.lib.fsm.internal.synchronization;

import com.skydo.lib.fsm.internal.tools.Pair;
import org.hibernate.action.spi.BeforeTransactionCompletionProcess;
import org.hibernate.engine.spi.SessionImplementor;

import java.util.HashMap;
import java.util.Map;

public class FSMProcess implements BeforeTransactionCompletionProcess {

    private final SessionImplementor session;

    private final Map<Pair<String, Object>, Object[]> entityStateCache;

    public FSMProcess(SessionImplementor session) {
        this.session = session;
        this.entityStateCache = new HashMap<>();
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

    @Override
    public void doBeforeTransactionCompletion(SessionImplementor session) {

    }
}
