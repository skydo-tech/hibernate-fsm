package com.skydo.lib.fsm.listener;

import com.skydo.lib.fsm.internal.FSMService;
import com.skydo.lib.fsm.internal.synchronization.FSMProcess;
import com.skydo.lib.fsm.internal.synchronization.work.FSMTransitionWork;
import com.skydo.lib.fsm.internal.synchronization.work.FSMWorkUnit;
import com.skydo.lib.fsm.internal.tools.StateMachine;
import com.skydo.lib.fsm.internal.tools.Transition;
import org.hibernate.Transaction;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.persister.entity.EntityPersister;

import java.util.Arrays;
import java.util.logging.Logger;

public class FSMPostUpdateListener extends BaseEventListener implements PostUpdateEventListener {

    private final Logger log = Logger.getLogger(FSMPostUpdateListener.class.getSimpleName());

    public FSMPostUpdateListener(FSMService fsmService) {
        super(fsmService);
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
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
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        return false;
    }
}
