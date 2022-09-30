package com.skydo.lib.fsm.internal.synchronization.work;

import com.skydo.lib.fsm.internal.FSMService;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.entity.EntityPersister;

import java.io.Serializable;

public abstract class AbstractFsmWorkUnit implements FSMWorkUnit {

    protected final SessionImplementor sessionImplementor;
    protected final FSMService fsmService;
    protected final Serializable id;
    protected final String entityName;

    private final EntityPersister entityPersister;

    protected AbstractFsmWorkUnit(
            SessionImplementor sessionImplementor,
            String entityName,
            FSMService fsmService,
            Serializable id,
            EntityPersister entityPersister
    ) {
        this.sessionImplementor = sessionImplementor;
        this.fsmService = fsmService;
        this.id = id;
        this.entityName = entityName;
        this.entityPersister = entityPersister;
    }

    @Override
    public Serializable getEntityId() {
        return id;
    }

    @Override
    public String getEntityName() {
        return entityName;
    }

    public EntityPersister getEntityPersister() {
        return entityPersister;
    }

    public SessionImplementor getSessionImplementor() {
        return sessionImplementor;
    }

    public FSMService getFsmService() {
        return fsmService;
    }
}
