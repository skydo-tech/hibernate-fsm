package com.skydo.lib.fsm.config;

import com.skydo.lib.fsm.definitions.postupdate.PostUpdateAction;
import com.skydo.lib.fsm.definitions.postupdate.PostUpdateActionHandler;
import com.skydo.lib.fsm.definitions.validator.TransitionValidator;
import com.skydo.lib.fsm.definitions.validator.TransitionValidatorHandler;
import com.skydo.lib.fsm.internal.tools.Pair;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SpringConfiguration implements BeanPostProcessor {

    final static HashMap<Class<?>, HashMap<String, HashMap<String, Pair<Object, Method>>>> entityToFieldMap
        = new HashMap<>();

    final static HashMap<Class<?>, HashMap<String, HashMap<String, Pair<Object, Method>>>> entityToPostActionMaps
        = new HashMap<>();

    private void processTransitionValidator(Object bean, String beanName) {
        if (bean.getClass().isAnnotationPresent(TransitionValidatorHandler.class)) {
            TransitionValidatorHandler transitionValidatorHandler = bean.getClass().getAnnotation(
                    TransitionValidatorHandler.class
            );
            Class<?> entityClass = transitionValidatorHandler.entity();
            if (!entityToFieldMap.containsKey(entityClass)) {
                entityToFieldMap.put(entityClass, new HashMap<>());
            }
            HashMap<String, HashMap<String, Pair<Object, Method>>> fieldToValuesMap = entityToFieldMap.get(entityClass);

            String entityField = transitionValidatorHandler.field();
            if (!fieldToValuesMap.containsKey(entityField)) {
                fieldToValuesMap.put(entityField, new HashMap<>());
            }

            HashMap<String, Pair<Object, Method>> valuesToValidators = fieldToValuesMap.get(entityField);
            List<Method> validatorMethod = Arrays.stream(bean.getClass().getMethods()).filter(
                f -> f.isAnnotationPresent(TransitionValidator.class)
            ).toList();
            validatorMethod.forEach(method -> {
                TransitionValidator transitionValidator = method.getAnnotation(TransitionValidator.class);
                String state = transitionValidator.state();
                valuesToValidators.put(state, Pair.make(bean, method));
            });
        }
    }

    private void processPostCommitAction(Object bean, String beanName) {
        if (bean.getClass().isAnnotationPresent(PostUpdateActionHandler.class)) {
            PostUpdateActionHandler postUpdateActionHandler = bean.getClass().getAnnotation(
                    PostUpdateActionHandler.class
            );
            Class<?> entityClass = postUpdateActionHandler.entity();
            if (!entityToPostActionMaps.containsKey(entityClass)) {
                entityToPostActionMaps.put(entityClass, new HashMap<>());
            }
            HashMap<String, HashMap<String, Pair<Object, Method>>> fieldToValuesMap =
                entityToPostActionMaps.get(entityClass);

            String entityField = postUpdateActionHandler.field();
            if (!fieldToValuesMap.containsKey(entityField)) {
                fieldToValuesMap.put(entityField, new HashMap<>());
            }

            HashMap<String, Pair<Object, Method>> valuesToPostCommitActions = fieldToValuesMap.get(entityField);
            List<Method> validatorMethod = Arrays.stream(bean.getClass().getMethods()).filter(
                f -> f.isAnnotationPresent(PostUpdateAction.class)
            ).toList();
            validatorMethod.forEach(method -> {
                PostUpdateAction postUpdateAction = method.getAnnotation(PostUpdateAction.class);
                String state = postUpdateAction.state();
                valuesToPostCommitActions.put(state, Pair.make(bean, method));
            });
        }
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        this.processPostCommitAction(bean, beanName);
        this.processTransitionValidator(bean, beanName);
        return bean;
    }
}
