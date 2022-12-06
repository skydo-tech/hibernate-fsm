package com.skydo.lib.fsm.servicecontributor;

import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.spi.ServiceContributor;

public class FSMServiceContributor implements ServiceContributor {
    @Override
    public void contribute(StandardServiceRegistryBuilder serviceRegistryBuilder) {
        serviceRegistryBuilder.addInitiator(FSMServiceInitiator.INSTANCE);
    }
}
