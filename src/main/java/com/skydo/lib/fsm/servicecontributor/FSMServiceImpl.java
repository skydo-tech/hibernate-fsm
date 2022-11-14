package com.skydo.lib.fsm.servicecontributor;

import com.skydo.lib.fsm.config.StateValidatorConfig;
import com.skydo.lib.fsm.internal.EntitiesConfigurations;
import com.skydo.lib.fsm.internal.EntitiesConfigurator;
import com.skydo.lib.fsm.internal.synchronization.FSMProcessManager;
import org.hibernate.annotations.common.reflection.ReflectionManager;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.spi.Configurable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class FSMServiceImpl implements FSMService, Configurable {

    private final static Logger log = LoggerFactory.getLogger(FSMServiceImpl.class);

    private boolean initialized = false;

    private ServiceRegistry serviceRegistry;

    private EntitiesConfigurations entitiesConfigurations;

    private FSMProcessManager fsmProcessManager;

    private final StateValidatorConfig stateValidatorConfig = new StateValidatorConfig();

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void initialize(MetadataImplementor metadata) {

        log.info("Initialize called");

        this.stateValidatorConfig.createValidatorMap();

        this.serviceRegistry = metadata.getMetadataBuildingOptions().getServiceRegistry();

        final ReflectionManager reflectionManager = metadata.getMetadataBuildingOptions().getReflectionManager();

        this.entitiesConfigurations = new EntitiesConfigurator().configure(
            metadata,
            serviceRegistry,
            reflectionManager
        );

        fsmProcessManager = new FSMProcessManager();

        initialized = true;
    }

    @Override
    public StateValidatorConfig getStateValidator() {
        return stateValidatorConfig;
    }


    @Override
    public void configure(Map map) {
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
