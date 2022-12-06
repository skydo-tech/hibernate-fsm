package com.skydo.lib.fsm.listener;

import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;

import java.util.logging.Logger;

public class FSMPreInsertListener implements PreInsertEventListener {

    private Logger log = Logger.getLogger(FSMPreInsertListener.class.getSimpleName());

    @Override
    public boolean onPreInsert(PreInsertEvent event) {
        final String entityName = event.getPersister().getEntityName();
        // TODO: What does true/false mean here? For some reason the return type of post<Event> is void
        return false;
    }
}
