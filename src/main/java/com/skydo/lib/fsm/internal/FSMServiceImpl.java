package com.skydo.lib.fsm.internal;

import com.skydo.lib.fsm.definitions.StateMachineHandler;
import com.skydo.lib.fsm.definitions.StateMachineHandlerMethod;
import com.skydo.lib.fsm.internal.scanner.TestingScanner;
import com.skydo.lib.fsm.internal.synchronization.FSMProcessManager;
import com.skydo.lib.fsm.internal.tools.StateMachine;
import com.skydo.lib.fsm.internal.tools.StateTransitionValidator;
import org.hibernate.annotations.common.reflection.ReflectionManager;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.spi.Configurable;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class FSMServiceImpl implements FSMService, Configurable {

    private boolean initialized = false;

    private ServiceRegistry serviceRegistry;

    private EntitiesConfigurations entitiesConfigurations;

    private FSMProcessManager fsmProcessManager;

    private final static Logger log = Logger.getLogger(FSMServiceImpl.class.getSimpleName());

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void initialize(MetadataImplementor metadata) {
        log.info("Initialize called");

        this.serviceRegistry = metadata.getMetadataBuildingOptions().getServiceRegistry();

        final ReflectionManager reflectionManager = metadata.getMetadataBuildingOptions().getReflectionManager();

        this.entitiesConfigurations = new EntitiesConfigurator().configure(
                metadata,
                serviceRegistry,
                reflectionManager
        );

        fsmProcessManager = new FSMProcessManager();

        new TestingScanner().doScan();

        initialized = true;
    }


    @Override
    public void configure(Map configurationValues) {
        log.info("Configure called");
    }

    public EntitiesConfigurations getEntitiesConfigurations() {
        return entitiesConfigurations;
    }

    @Override
    public FSMProcessManager getFsmProcessManager() {
        return fsmProcessManager;
    }
}
