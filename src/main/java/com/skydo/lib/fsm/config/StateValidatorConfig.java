package com.skydo.lib.fsm.config;

import com.skydo.lib.fsm.definitions.StateTransition;
import com.skydo.lib.fsm.definitions.TransitionValidator;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;

public class StateValidatorConfig {

    private final Logger log = LoggerFactory.getLogger(StateValidatorConfig.class.getSimpleName());

    private final HashMap<Class<?>, HashMap<String, HashMap<String, Method>>> entityToFieldMap = new HashMap<>();

    public HashMap<Class<?>, HashMap<String, HashMap<String, Method>>> getValidatorMap() {
        return entityToFieldMap;
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

            if (!entityToFieldMap.containsKey(entity)) {
                entityToFieldMap.put(entity, new HashMap<>());
            }

            HashMap<String, HashMap<String, Method>> fieldToValuesMap = entityToFieldMap.get(entity);

            if (!fieldToValuesMap.containsKey(field)) {
                fieldToValuesMap.put(field, new HashMap<>());
            }

            HashMap<String, Method> valuesToValidators = fieldToValuesMap.get(field);


            Method[] validators = validatorClass.getDeclaredMethods();

            for (Method validator : validators) {

                TransitionValidator validatorAnnotation = validator.getAnnotation(TransitionValidator.class);

                String fieldValue = validatorAnnotation.state();

                valuesToValidators.put(fieldValue, validator);

            }


        }

    }

}


















