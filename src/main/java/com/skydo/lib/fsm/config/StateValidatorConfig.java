package com.skydo.lib.fsm.config;

import com.skydo.lib.fsm.internal.tools.Pair;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

public class StateValidatorConfig {

//    private final Logger log = LoggerFactory.getLogger(StateValidatorConfig.class.getSimpleName());

    public HashMap<Class<?>, HashMap<String, HashMap<String, Pair<Object, List<Method>>>>> getValidatorMap() {
        return SpringConfiguration.entityToFieldMap;

    /**
     * e.g. Exporter
       {
            key: class com.example.fsmwrapper.repository.Exporter,
            value: ({
              key: "onboardingState" -> {HashMap@12865}  size = 2,
              value: ( size = 2
                { key: "ONBOARDING_COMPLETE", value: method_1 (validator) },
                { key: "KYC_COMPLETE", value: method_2 (validator) }
              )
            })
        }
     */
    /**
     * e.g. Exporter
       {
            key: class com.example.fsmwrapper.repository.Exporter,
            value: ({
              key: "onboardingState" -> {HashMap@12865}  size = 1,
              value: ( size = 1
                { key: "ONBOARDING_COMPLETE", value: method_1 (post update action) },
              )
            })
        }
     */
    }

    public HashMap<Class<?>, HashMap<String, HashMap<String, Pair<Object, List<Method>>>>> getEntityFieldPostUpdateActionMap() {
        return SpringConfiguration.entityToPostActionMaps;
    }

    public void createEntityFieldValidatorMap() {}

    public void createEntityFieldPostActionMap() {}

    public void createEntityFieldMaps() {
        this.createEntityFieldValidatorMap();
        this.createEntityFieldPostActionMap();
    }
}
