package com.skydo.lib.fsm.listenerv2;

import com.skydo.lib.fsm.config.StateValidator;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class FSMPostUpdateListener implements PostUpdateEventListener {

    private final Logger log = LoggerFactory.getLogger(FSMPostUpdateListener.class.getSimpleName());

    private final StateValidator stateValidator;

    public FSMPostUpdateListener(StateValidator stateValidator) {
        this.stateValidator = stateValidator;
    }


    @Override
    public void onPostUpdate(PostUpdateEvent postUpdateEvent) {

        HashMap<Class<?>, HashMap<String, HashMap<String, Method>>> validatorMap = stateValidator.getValidatorMap();

        Object entity = postUpdateEvent.getEntity();

        Class<?> entityClass = entity.getClass();

        if(validatorMap.containsKey(entityClass)) {

            String[] propertyNames = postUpdateEvent.getPersister().getPropertyNames();

            int[] dirtyProperties = postUpdateEvent.getDirtyProperties();

            for(int propertyIndex : dirtyProperties) {

                String propertyName = propertyNames[propertyIndex];

                HashMap<String, HashMap<String, Method>> fieldToValuesMap = validatorMap.get(entityClass);

                if(fieldToValuesMap.containsKey(propertyName)) {

                    String newValue = postUpdateEvent.getState()[propertyIndex].toString();

                    HashMap<String, Method> valuesToValidators = fieldToValuesMap.get(propertyName);

                    if(valuesToValidators.containsKey(newValue)) {

                        Method validator = valuesToValidators.get(newValue);

                        Class<?> declaringClass = validator.getDeclaringClass();

                        try {
                            validator.invoke(declaringClass.getConstructors()[0].newInstance(), entity);
                        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                            throw new RuntimeException(e);
                        }

                    }


                }


            }

        }

    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister entityPersister) {
        return false;
    }
}



















