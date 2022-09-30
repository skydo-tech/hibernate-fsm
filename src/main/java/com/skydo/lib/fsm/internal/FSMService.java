package com.skydo.lib.fsm.internal;

import com.skydo.lib.fsm.internal.synchronization.FSMProcessManager;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.service.Service;

public interface FSMService extends Service {

    boolean isInitialized();

    void initialize(MetadataImplementor metadata);

    EntitiesConfigurations getEntitiesConfigurations();

    FSMProcessManager getFsmProcessManager();

}
