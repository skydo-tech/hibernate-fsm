package com.skydo.lib.fsm.internal.tools;

import java.util.List;
import java.util.Map;

public interface StateTransition {

    Map<String, List<String>> getConfig();

}
