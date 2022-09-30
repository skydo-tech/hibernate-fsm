package com.skydo.lib.fsm.internal.synchronization;

import org.hibernate.Transaction;
import org.hibernate.action.spi.AfterTransactionCompletionProcess;
import org.hibernate.action.spi.BeforeTransactionCompletionProcess;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.event.spi.EventSource;

import java.util.HashMap;
import java.util.Map;

public class FSMProcessManager {

    private final Map<Transaction, FSMProcess> fsmProcessesList;

    public FSMProcessManager() {
        this.fsmProcessesList = new HashMap<>();
    }

    public FSMProcess get(EventSource session) {
        final Transaction transaction = session.accessTransaction();

        FSMProcess fsmProcess = fsmProcessesList.get( transaction );
        if ( fsmProcess == null ) {
            // No worries about registering a transaction twice - a transaction is single thread
            fsmProcess = new FSMProcess( session );
            fsmProcessesList.put( transaction, fsmProcess );

            session.getActionQueue().registerProcess(
                    new BeforeTransactionCompletionProcess() {
                        public void doBeforeTransactionCompletion(SessionImplementor session) {
                            final FSMProcess process = fsmProcessesList.get( transaction );
                            if ( process != null ) {
                                process.doBeforeTransactionCompletion( session );
                            }
                        }
                    }
            );

            session.getActionQueue().registerProcess(
                    new AfterTransactionCompletionProcess() {
                        public void doAfterTransactionCompletion(boolean success, SharedSessionContractImplementor session) {
                            fsmProcessesList.remove( transaction );
                        }
                    }
            );
        }

        return fsmProcess;
    }
}
