package com.skydo.lib.fsm.definitions.postupdate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Raj Sheth
 */


/**
 * To use declare a class and annotate with `PostUpdateActionHandler`
 * Define postUpdate Methods inside that class and annotate it with `PostUpdateAction`.
 * Make sure to pass correct values for following fields.
 * 1. entity: Database entity
 * 2. field: On which state configuration is made
 * 3. state: `field.value` on which this action should be triggered
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PostUpdateActionHandler {
    Class<?> entity();

    String field();
}
