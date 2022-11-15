package com.skydo.lib.fsm.config;

import com.skydo.lib.fsm.definitions.postupdate.PostUpdateAction;
import com.skydo.lib.fsm.definitions.postupdate.PostUpdateActionHandler;
import com.skydo.lib.fsm.definitions.validator.TransitionValidatorHandler;
import com.skydo.lib.fsm.definitions.validator.TransitionValidator;
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
    private final HashMap<Class<?>, HashMap<String, HashMap<String, Method>>> entityFieldValidatorMap = new HashMap<>();

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
    private final HashMap<Class<?>, HashMap<String, HashMap<String, Method>>> entityFieldPostUpdateActionMap
            = new HashMap<>();

    public HashMap<Class<?>, HashMap<String, HashMap<String, Method>>> getEntityFieldValidatorMap() {
        return entityFieldValidatorMap;
    }

    public HashMap<Class<?>, HashMap<String, HashMap<String, Method>>> getEntityFieldPostUpdateActionMap() {
        return entityFieldPostUpdateActionMap;
    }

    public void createEntityFieldValidatorMap() {
        log.info("Creating Entity Field Validator Map");
        Set<String> stateMachinePackageScanConfig = EnableStateMachinePackages.Registrar.stateMachinePackageScanConfig;
        log.info(String.join(",", stateMachinePackageScanConfig));
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackages(StringUtils.toStringArray(stateMachinePackageScanConfig))
                .setScanners(Scanners.MethodsAnnotated, Scanners.TypesAnnotated));

        Set<Class<?>> validatorClasses =
                reflections.get(Scanners.TypesAnnotated.with(TransitionValidatorHandler.class).asClass());
        for (Class<?> validatorClass: validatorClasses) {
            TransitionValidatorHandler stateMachineAnnotation = validatorClass.getAnnotation(TransitionValidatorHandler.class);
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
            for (Method validator: validators) {
                Annotation[] methodAnnotations = validator.getAnnotations();
                /**
                 * Only consider those methods which are annotated with `TransitionValidator`
                 */
                if (Arrays.stream(methodAnnotations).anyMatch(
                        annotation -> annotation.annotationType().equals(TransitionValidator.class)
                )) {
                    TransitionValidator validatorAnnotation = validator.getAnnotation(TransitionValidator.class);
                    String fieldValue = validatorAnnotation.state();
                    valuesToValidators.put(fieldValue, validator);
                }
            }
        }
    }

    public void createEntityFieldPostActionMap() {
        log.info("Creating Entity Field PostUpdate Action Map");
        Set<String> stateMachinePackageScanConfig = EnableStateMachinePackages.Registrar.stateMachinePackageScanConfig;
        log.info(String.join(",", stateMachinePackageScanConfig));
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackages(StringUtils.toStringArray(stateMachinePackageScanConfig))
                .setScanners(Scanners.MethodsAnnotated, Scanners.TypesAnnotated));

        Set<Class<?>> postActionHandlerClazzes =
                reflections.get(Scanners.TypesAnnotated.with(PostUpdateActionHandler.class).asClass());

        for (Class<?> postActionHandlerClazz: postActionHandlerClazzes) {
            PostUpdateActionHandler postUpdateActionHandler
                = postActionHandlerClazz.getAnnotation(PostUpdateActionHandler.class);
            Class<?> entity = postUpdateActionHandler.entity();
            String field = postUpdateActionHandler.field();

            if (!entityFieldPostUpdateActionMap.containsKey(entity)) {
                entityFieldPostUpdateActionMap.put(entity, new HashMap<>());
            }

            HashMap<String, HashMap<String, Method>> fieldToValuesMap = entityFieldPostUpdateActionMap.get(entity);
            if (!fieldToValuesMap.containsKey(field)) {
                fieldToValuesMap.put(field, new HashMap<>());
            }
            HashMap<String, Method> valuesToPostActions = fieldToValuesMap.get(field);
            Method[] postActionMethods = postActionHandlerClazz.getDeclaredMethods();
            for (Method postActionMethod: postActionMethods) {
                Annotation[] methodAnnotations = postActionMethod.getAnnotations();
                /**
                 * Only consider those methods which are annotated with `PostUpdateAction`
                 */
                if (Arrays.stream(methodAnnotations).anyMatch(
                        annotation -> annotation.annotationType().equals(PostUpdateAction.class)
                )) {
                    PostUpdateAction postUpdateAction = postActionMethod.getAnnotation(PostUpdateAction.class);
                    String fieldValue = postUpdateAction.state();
                    valuesToPostActions.put(fieldValue, postActionMethod);
                }
            }
        }
    }

    public void createEntityFieldMaps() {
        this.createEntityFieldValidatorMap();
        this.createEntityFieldPostActionMap();
    }
}
