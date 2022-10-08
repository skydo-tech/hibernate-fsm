package com.skydo.lib.fsm.listener;

import com.skydo.lib.fsm.servicecontributor.FSMService;
import com.skydo.lib.fsm.internal.synchronization.FSMProcess;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;

public class FSMPreUpdateListener extends BaseEventListener implements PreUpdateEventListener {

    public FSMPreUpdateListener(FSMService fsmService) {
        super(fsmService);
    }

    @Override
    public boolean onPreUpdate(PreUpdateEvent event) {
        final String entityName = event.getPersister().getEntityName();

        if (getFsmService().getEntitiesConfigurations().isStateManaged(entityName)) {
            checkIfTransactionInProgress(event.getSession());

            Object[] currentState = event.getPersister().getDatabaseSnapshot(event.getId(), event.getSession());

            final FSMProcess fsmProcess = getFsmService().getFsmProcessManager().get(event.getSession());
            fsmProcess.cacheEntityState(event.getId(), entityName, currentState);
        }
        return false;
    }
}
