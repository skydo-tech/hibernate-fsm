package com.skydo.lib.fsm.servicecontributor;

import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.service.Service;

public interface FSMService extends Service {
    boolean isInitialized();

    void initialize(MetadataImplementor metadata);
}
