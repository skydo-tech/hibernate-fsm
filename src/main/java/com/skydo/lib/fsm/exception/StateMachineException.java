package com.skydo.lib.fsm.exception;

import javax.persistence.PersistenceException;

public class StateMachineException extends PersistenceException {

    public StateMachineException() {
    }

    public StateMachineException(String message) {
        super(message);
    }

    public StateMachineException(String message, Throwable cause) {
        super(message, cause);
    }

    public StateMachineException(Throwable cause) {
        super(cause);
    }
}
