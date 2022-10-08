package com.skydo.lib.fsm.config;

import com.skydo.lib.fsm.definitions.StateMachineHandler;
import com.skydo.lib.fsm.definitions.StateMachineHandlerMethod;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class StateValidator {

    private final Logger log = LoggerFactory.getLogger(StateValidator.class.getSimpleName());

    private final HashMap<Class<?>, HashMap<String, HashMap<String, Method> > > entityToFieldMap = new HashMap<>();

    public HashMap<Class<?>, HashMap<String, HashMap<String, Method> > > getValidatorMap() {
        return entityToFieldMap;
    }

    public void createValidatorMap() {

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackage("com.skydo.hibernateevents")
                .setScanners(Scanners.MethodsAnnotated, Scanners.TypesAnnotated));

        Set<Class<?>> validatorClasses =
                reflections.get(Scanners.TypesAnnotated.with(StateMachineHandler.class).asClass());

        for (Class<?> validatorClass : validatorClasses) {

            StateMachineHandler stateMachineAnnotation = validatorClass.getAnnotation(StateMachineHandler.class);

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

                StateMachineHandlerMethod validatorAnnotation = validator.getAnnotation(StateMachineHandlerMethod.class);

                String fieldValue = validatorAnnotation.fieldValue();

                valuesToValidators.put(fieldValue, validator);

            }


        }

    }

}


















