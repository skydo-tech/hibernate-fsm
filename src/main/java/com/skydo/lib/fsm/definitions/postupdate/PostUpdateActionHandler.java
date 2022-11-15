package com.skydo.lib.fsm.definitions.postupdate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Raj Sheth
 */


/**
 * Class level config
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PostUpdateActionHandler {
    Class<?> entity();

    String field();
}
