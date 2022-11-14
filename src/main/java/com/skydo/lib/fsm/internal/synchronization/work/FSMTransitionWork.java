package com.skydo.lib.fsm.internal.synchronization.work;

import com.skydo.lib.fsm.definitions.FSMEntityType;
import com.skydo.lib.fsm.exception.StateMachineException;
import com.skydo.lib.fsm.exception.StateValidationException;
import com.skydo.lib.fsm.servicecontributor.FSMService;
import com.skydo.lib.fsm.internal.tools.StateMachine;
import com.skydo.lib.fsm.internal.tools.StateTransition;
import com.skydo.lib.fsm.internal.tools.StateTransitionVoid;
import com.skydo.lib.fsm.internal.tools.Transition;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.persister.entity.EntityPersister;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FSMTransitionWork extends AbstractFsmWorkUnit {

    private final Object[] oldState;

    private final Object[] newState;

    public FSMTransitionWork(
            SessionImplementor sessionImplementor,
            String entityName,
            FSMService fsmService,
            Serializable id,
            EntityPersister entityPersister,
            Object[] oldState,
            Object[] newState
    ) {
        super(sessionImplementor, entityName, fsmService, id, entityPersister);
        this.oldState = oldState;
        this.newState = newState;
    }

    @Override
    public void perform() {
        for (int i = 0; i < getEntityPersister().getPropertyNames().length; i++) {
            String propertyName = getEntityPersister().getPropertyNames()[i];
            boolean isPropertyStateful = getFsmService().getEntitiesConfigurations().isStateManaged(entityName, propertyName);
            if (isPropertyStateful) {
                String newValue = convertPropertyToString(newState[i]);
                XProperty property = getFsmService().getEntitiesConfigurations().get(entityName, propertyName);
                StateMachine stateMachine = property.getAnnotation(StateMachine.class);
                Transition[] transitions = stateMachine.config();
                String initialState = stateMachine.initialState();

                /**
                 * If `oldState` is empty array, it must be a PostInsert event
                 */
                if (oldState.length == 0) {
                    /**
                     * initial state is not defined, ignore and let it pass
                     */
                    if (initialState.equals("")) {
                        return;
                    }
                    if (!newValue.equals(initialState)) {
                        throw new StateValidationException(
                            "For entity: " + entityName + " invalid `initialState`: "
                                + newValue + ". It must be " + initialState
                        );
                    }
                    /**
                     * otherwise looks good
                     */
                    return;
                }

                String oldValue = convertPropertyToString(oldState[i]);
                if (oldValue.equals(newValue)) {
                    // This transition is allowed (No change in the state)
                    return;
                }

                if (transitions.length > 0) {
                    this.checkTransition(oldValue, newValue, transitions);
                } else if (stateMachine.classConfig() != StateTransitionVoid.class) {
                    this.checkTransition(oldValue, newValue, stateMachine.classConfig());
                } else {
                    throw new StateMachineException("No proper config defined for " + entityName + ", property " + propertyName);
                }
            }
        }
    }

    private void checkTransition(String oldValue, String newValue, Transition[] transitions) {
        boolean isTransitionAllowed = false;

        for (Transition transition :transitions) {
            if (transition.fromState().equals(oldValue)) {
                if (Arrays.asList(transition.toState()).contains(newValue)) {
                    // This transition is allowed
                    isTransitionAllowed = true;
                }
            }
        }

        if (!isTransitionAllowed) {
            throw new StateMachineException(entityName + " Transition from " + oldValue + " to " + newValue + " is not allowed");
        }
    }

    private void checkTransition(String oldValue, String newValue, Class<? extends StateTransition> transitionConfigClass) {
        try {
            Map<String, List<String>> config = transitionConfigClass.getConstructor().newInstance().getConfig();
            List<String> allowedStates = config.get(oldValue);
            if (allowedStates == null || !allowedStates.contains(newValue)) {
                throw new StateMachineException(entityName + " Transition from " + oldValue + " to " + newValue + " is not allowed");
            }
        } catch (Exception e) {
            throw new StateMachineException(e);
        }
    }

    private String convertPropertyToString(Object property) {
        if (property instanceof Enum<?>) {
            return property.toString();
        } else if (property instanceof String) {
            return(String) property;
        }
        throw new StateMachineException("State managed property data type cannot be determined");
    }
}
