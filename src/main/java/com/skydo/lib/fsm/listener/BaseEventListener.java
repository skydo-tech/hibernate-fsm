package com.skydo.lib.fsm.listener;

import com.skydo.lib.fsm.servicecontributor.FSMService;
import org.hibernate.engine.spi.SessionImplementor;

public abstract class BaseEventListener {

    private final FSMService fsmService;

    protected BaseEventListener(FSMService fsmService) {
        this.fsmService = fsmService;
    }

    protected void checkIfTransactionInProgress(SessionImplementor session) {
        if ( !session.isTransactionInProgress() ) {
            // Historical data would not be flushed to audit tables if outside of active transaction
            // (AuditProcess#doBeforeTransactionCompletion(SessionImplementor) not executed).
            throw new RuntimeException( "Unable to create revision because of non-active transaction" );
        }
    }

    public FSMService getFsmService() {
        return fsmService;
    }
}

