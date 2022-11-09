package com.skydo.lib.fsm.exception;

import javax.persistence.PersistenceException;

public class StateValidationException extends PersistenceException {
    public StateValidationException() {
    }

    public StateValidationException(String... message) {
        super(String.join(", ", message));
    }
}
