package com.skydo.lib.fsm.integrator;

import com.skydo.lib.fsm.listener.FSMPreInsertListener;
import com.skydo.lib.fsm.listener.FSMPreUpdateListener;
import com.skydo.lib.fsm.listener.FSMPostUpdateListener;
import com.skydo.lib.fsm.servicecontributor.FSMService;
import org.hibernate.HibernateException;
import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import java.util.logging.Logger;

public class FSMIntegrator implements Integrator {

    private static final Logger log = Logger.getLogger(FSMIntegrator.class.getSimpleName());


    @Override
    public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactoryImplementor, SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {

        final FSMService fsmService = sessionFactoryServiceRegistry.getService(FSMService.class);

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // Verify that the FSMService is fully initialized and ready to go.
        if ( !fsmService.isInitialized() ) {
            throw new HibernateException(
                    "Expecting FSMService to have been initialized prior to call to FSMIntegrator#integrate"
            );
        }

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // Opt-out of registration if no state managed entities found
        if ( !fsmService.getEntitiesConfigurations().hasEntities() ) {
            log.info( "Skipping Envers listener registrations : No audited entities found" );
            return;
        }

        final EventListenerRegistry listenerRegistry = sessionFactoryServiceRegistry.getService(EventListenerRegistry.class);

        listenerRegistry.appendListeners(
                EventType.PRE_INSERT,
                new FSMPreInsertListener()
        );

        listenerRegistry.appendListeners(
                EventType.POST_INSERT,
                new
        );

        listenerRegistry.appendListeners(
                EventType.PRE_UPDATE,
                new FSMPreUpdateListener(fsmService)
        );

        listenerRegistry.appendListeners(EventType.POST_UPDATE, new FSMPostUpdateListener(fsmService));

    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactoryImplementor, SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {
        log.info("Disintegrate!!");
    }
}
