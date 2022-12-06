package com.skydo.lib.fsm.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({EnableStateMachinePackages.Registrar.class})
public @interface EnableStateMachine {

    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};
}
