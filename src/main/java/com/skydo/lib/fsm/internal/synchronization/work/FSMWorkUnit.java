package com.skydo.lib.fsm.internal.synchronization.work;

import java.io.Serializable;

public interface FSMWorkUnit {

    Serializable getEntityId();

    String getEntityName();

    void perform();
}
