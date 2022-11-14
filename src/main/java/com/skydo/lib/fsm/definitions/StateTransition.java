package com.skydo.lib.fsm.definitions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used over a class where all the transition validation functions are declared.
 * Methods declared inside the consumer class will have `TransitionValidator` annotation
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StateTransition {

    Class<?> entity();

    String field();
}
