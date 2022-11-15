package com.skydo.lib.fsm.config;

import com.skydo.lib.fsm.definitions.StateTransition;
import com.skydo.lib.fsm.definitions.TransitionValidator;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class StateValidatorConfig {

    private final Logger log = LoggerFactory.getLogger(StateValidatorConfig.class.getSimpleName());

    private final HashMap<Class<?>, HashMap<String, HashMap<String, Method>>> entityFieldValidatorMap = new HashMap<>();

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
    public HashMap<Class<?>, HashMap<String, HashMap<String, Method>>> getValidatorMap() {
        return entityFieldValidatorMap;
    }

    public void createValidatorMap() {
        Set<String> stateMachinePackageScanConfig = EnableStateMachinePackages.Registrar.stateMachinePackageScanConfig;
        log.info(stateMachinePackageScanConfig.toString());
        log.info(String.join(",", stateMachinePackageScanConfig));
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackages(StringUtils.toStringArray(stateMachinePackageScanConfig))
                .setScanners(Scanners.MethodsAnnotated, Scanners.TypesAnnotated));

        Set<Class<?>> validatorClasses =
                reflections.get(Scanners.TypesAnnotated.with(StateTransition.class).asClass());
        for (Class<?> validatorClass : validatorClasses) {
            StateTransition stateMachineAnnotation = validatorClass.getAnnotation(StateTransition.class);
            Class<?> entity = stateMachineAnnotation.entity();
            String field = stateMachineAnnotation.field();
            if (!entityFieldValidatorMap.containsKey(entity)) {
                entityFieldValidatorMap.put(entity, new HashMap<>());
            }

            HashMap<String, HashMap<String, Method>> fieldToValuesMap = entityFieldValidatorMap.get(entity);
            if (!fieldToValuesMap.containsKey(field)) {
                fieldToValuesMap.put(field, new HashMap<>());
            }
            HashMap<String, Method> valuesToValidators = fieldToValuesMap.get(field);
            Method[] validators = validatorClass.getDeclaredMethods();
            for (Method validator : validators) {
                if (Arrays.stream(validator.getAnnotations()).anyMatch(
                        annotation -> annotation.annotationType().equals(TransitionValidator.class)
                )) {
                    TransitionValidator validatorAnnotation = validator.getAnnotation(TransitionValidator.class);
                    String fieldValue = validatorAnnotation.state();
                    valuesToValidators.put(fieldValue, validator);
                }
            }
        }
    }
}
