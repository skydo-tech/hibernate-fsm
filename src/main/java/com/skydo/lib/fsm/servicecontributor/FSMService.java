package com.skydo.lib.fsm.servicecontributor;

import com.skydo.lib.fsm.config.StateValidatorConfig;
import com.skydo.lib.fsm.internal.EntitiesConfigurations;
import com.skydo.lib.fsm.internal.synchronization.FSMProcessManager;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.service.Service;

public interface FSMService extends Service {
    boolean isInitialized();

    void initialize(MetadataImplementor metadata);

    StateValidatorConfig getStateValidator();

    EntitiesConfigurations getEntitiesConfigurations();

    FSMProcessManager getFsmProcessManager();

}
