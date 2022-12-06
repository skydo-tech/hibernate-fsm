package com.skydo.lib.fsm.definitions.postupdate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Raj Sheth
 */

/**
 * IMPORTANT: function signature which makes use of this annotation must follow a signature described below
 * While executing this function from this hibernate layer all these values will be passed.
 *
 * `fun postStateActionMethod(entityId: Long, oldFieldValue: Any, newFieldValue: Any)`
 *
 * This gets executed on both of the following events (outside of Transaction boundary)
 * 1. Post Update
 * 2. Post Insert: For post insert `oldValue` will always be empty (`""`) string
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PostUpdateAction {
	String state();
}
