package com.skydo.lib.fsm.internal;

import org.hibernate.boot.registry.StandardServiceInitiator;
import org.hibernate.service.spi.ServiceRegistryImplementor;

import java.util.Map;

public class FSMServiceInitiator implements StandardServiceInitiator<FSMService> {

    public static final FSMServiceInitiator INSTANCE = new FSMServiceInitiator();

    @Override
    public FSMService initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
        return new FSMServiceImpl();
    }

    @Override
    public Class<FSMService> getServiceInitiated() {
        return FSMService.class;
    }
}
