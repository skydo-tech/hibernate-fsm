package com.skydo.lib.fsm.config;

import com.skydo.lib.fsm.definitions.StateTransition;
import com.skydo.lib.fsm.definitions.TransitionValidator;
import com.skydo.lib.fsm.internal.tools.Pair;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class SpringConfiguration implements BeanPostProcessor {

    ApplicationContext applicationContext;

//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        this.applicationContext = applicationContext;
//    }


    final static HashMap<Class<?>, HashMap<String, HashMap<String, Pair<Object, Method>>>> entityToFieldMap = new HashMap<>();


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(StateTransition.class)) {
            StateTransition stateTransition = bean.getClass().getAnnotation(StateTransition.class);
            Class<?> entityClass = stateTransition.entity();
            if (!entityToFieldMap.containsKey(entityClass)) {
                entityToFieldMap.put(entityClass, new HashMap<>());
            }
            HashMap<String, HashMap<String, Pair<Object, Method>>> fieldToValuesMap = entityToFieldMap.get(entityClass);

            String entityField = stateTransition.field();
            if (!fieldToValuesMap.containsKey(entityField)) {
                fieldToValuesMap.put(entityField, new HashMap<>());
            }

            HashMap<String, Pair<Object, Method>> valuesToValidators = fieldToValuesMap.get(entityField);
            List<Method> validatorMethod = Arrays.stream(bean.getClass().getMethods()).filter(f -> f.isAnnotationPresent(TransitionValidator.class)).toList();
            validatorMethod.forEach(method -> {
                TransitionValidator transitionValidator = method.getAnnotation(TransitionValidator.class);
                String state = transitionValidator.state();
                valuesToValidators.put(state, Pair.make(bean, method));
            });

            System.out.println("asdkljasd");
        }
        return bean;
    }
}
