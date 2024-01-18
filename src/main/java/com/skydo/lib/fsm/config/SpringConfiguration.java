package com.skydo.lib.fsm.config;

import com.skydo.lib.fsm.definitions.postupdate.PostUpdateAction;
import com.skydo.lib.fsm.definitions.postupdate.PostUpdateActionHandler;
import com.skydo.lib.fsm.definitions.validator.TransitionValidator;
import com.skydo.lib.fsm.definitions.validator.TransitionValidatorHandler;
import com.skydo.lib.fsm.internal.tools.Pair;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class SpringConfiguration implements BeanPostProcessor {

    final static HashMap<Class<?>, HashMap<String, HashMap<String, Pair<Object, List<Method>>>>> entityToFieldMap
        = new HashMap<>();

    final static HashMap<Class<?>, HashMap<String, HashMap<String, Pair<Object, List<Method>>>>> entityToPostActionMaps
        = new HashMap<>();

    final static HashMap<Class<?>, HashMap<String, Pair<Object, List<Method>>>> entityToPostActionMapsGeneric
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
            HashMap<String, HashMap<String, Pair<Object, List<Method>>>> fieldToValuesMap = entityToFieldMap.get(entityClass);

            String entityField = transitionValidatorHandler.field();
            if (!fieldToValuesMap.containsKey(entityField)) {
                fieldToValuesMap.put(entityField, new HashMap<>());
            }

            HashMap<String, Pair<Object, List<Method>>> valuesToValidators = fieldToValuesMap.get(entityField);
            List<Method> validatorMethods = Arrays.stream(bean.getClass().getMethods()).filter(
                f -> f.isAnnotationPresent(TransitionValidator.class)
            ).collect(Collectors.toList());
            validatorMethods.forEach(method -> {
                TransitionValidator transitionValidator = method.getAnnotation(TransitionValidator.class);
                String state = transitionValidator.state();
                if (valuesToValidators.containsKey(state)) {
                    Pair<Object, List<Method>> currentMethodsObjectPair = valuesToValidators.get(state);
                    List<Method> methodsSoFar = currentMethodsObjectPair.getSecond();
                    methodsSoFar.add(method);
                    valuesToValidators.remove(state);
                    valuesToValidators.put(state, Pair.make(bean, methodsSoFar));
                } else {
                    List<Method> methods = new ArrayList<Method>();
                    methods.add(method);
                    valuesToValidators.put(state, Pair.make(bean, methods));
                }
            });
        }
    }

    private void processPostCommitActions(Object bean, String beanName) {
        if (bean.getClass().isAnnotationPresent(PostUpdateActionHandler.class)) {
            PostUpdateActionHandler postUpdateActionHandler = bean.getClass().getAnnotation(
                    PostUpdateActionHandler.class
            );
            Class<?> entityClass = postUpdateActionHandler.entity();
            if (!entityToPostActionMaps.containsKey(entityClass)) {
                entityToPostActionMaps.put(entityClass, new HashMap<>());
            }
            HashMap<String, HashMap<String, Pair<Object, List<Method>>>> fieldToValuesMap =
                entityToPostActionMaps.get(entityClass);

            String entityField = postUpdateActionHandler.field();
            if (!fieldToValuesMap.containsKey(entityField)) {
                fieldToValuesMap.put(entityField, new HashMap<>());
            }

            HashMap<String, Pair<Object, List<Method>>> valuesToPostCommitActions = fieldToValuesMap.get(entityField);
            List<Method> postCommitActionMethods = Arrays.stream(bean.getClass().getMethods()).filter(
                f -> f.isAnnotationPresent(PostUpdateAction.class)
            ).collect(Collectors.toList());
            postCommitActionMethods.forEach(method -> {
                PostUpdateAction postUpdateAction = method.getAnnotation(PostUpdateAction.class);
                String state = postUpdateAction.state();
                if (state.equals("*")) {
                    if (!entityToPostActionMapsGeneric.containsKey(entityClass)) {
                        entityToPostActionMapsGeneric.put(entityClass, new HashMap<>());
                    }
                    HashMap<String, Pair<Object, List<Method>>> fieldToActionsMap = entityToPostActionMapsGeneric.get(entityClass);
                    if (!fieldToActionsMap.containsKey(entityField)) {
                        List<Method> methods = new ArrayList<Method>();
                        methods.add(method);
                        fieldToActionsMap.put(entityField, Pair.make(bean, methods));
                    } else {
                        Pair<Object, List<Method>> currentMethodsObjectPair = fieldToActionsMap.get(entityField);
                        List<Method> methodsSoFar = currentMethodsObjectPair.getSecond();
                        methodsSoFar.add(method);
                        fieldToActionsMap.remove(entityField);
                        fieldToActionsMap.put(entityField, Pair.make(bean, methodsSoFar));
                    }
                } else {
                    if (valuesToPostCommitActions.containsKey(state)) {
                        Pair<Object, List<Method>> currentMethodsObjectPair = valuesToPostCommitActions.get(state);
                        List<Method> methodsSoFar = currentMethodsObjectPair.getSecond();
                        methodsSoFar.add(method);
                        valuesToPostCommitActions.remove(state);
                        valuesToPostCommitActions.put(state, Pair.make(bean, methodsSoFar));
                    } else {
                        List<Method> methods = new ArrayList<Method>();
                        methods.add(method);
                        valuesToPostCommitActions.put(state, Pair.make(bean, methods));
                    }
                }
            });
        }
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        this.processPostCommitActions(bean, beanName);
        this.processTransitionValidator(bean, beanName);
        return bean;
    }
}
