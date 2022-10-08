package com.skydo.lib.fsm.integrator;

import com.skydo.lib.fsm.servicecontributor.FSMService;
import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import java.util.logging.Logger;

public class FSMIntegrator implements Integrator {

    private static final Logger log = Logger.getLogger(FSMIntegrator.class.getSimpleName());


    @Override
    public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactoryImplementor, SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {
        final FSMService fsmService = sessionFactoryServiceRegistry.getService(FSMService.class);
        final EventListenerRegistry listenerRegistry = sessionFactoryServiceRegistry.getService(EventListenerRegistry.class);
        log.info("..." + fsmService + " .... " + listenerRegistry);
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactoryImplementor, SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {
        log.info("Disintegrate!!");
    }
}
