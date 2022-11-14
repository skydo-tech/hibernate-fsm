package com.skydo.lib.fsm.listener;

import com.skydo.lib.fsm.internal.synchronization.work.FSMTransitionWork;
import com.skydo.lib.fsm.internal.synchronization.work.FSMWorkUnit;
import com.skydo.lib.fsm.servicecontributor.FSMService;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.persister.entity.EntityPersister;

import java.util.logging.Logger;

public class FSMPostInsertListener extends BaseEventListener implements PostInsertEventListener {

    private static final Logger log = Logger.getLogger(FSMPostInsertListener.class.getSimpleName());

    public FSMPostInsertListener(FSMService fsmService) {
        super(fsmService);
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {
        final String entityName = event.getPersister().getEntityName();
        log.info("Inside postInsert event. entityName = " + entityName);

        // TODO: Do we need the following line?
        // checkIfTransactionInProgress(event.getSession());

        /**
         * As it is insert, creating an empty array for oldState
         * Useful for checking valid initial state inside workUnit.perform()
         */
        Object[] oldState = new Object[]{};

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

    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        return false;
    }
}
