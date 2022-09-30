package com.skydo.lib.fsm.internal.scanner;

import com.skydo.lib.fsm.internal.tools.StateTransitionValidator;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.logging.Logger;

public class TestingScanner {

    private static Logger log = Logger.getLogger(TestingScanner.class.getSimpleName());

    public void doScan() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackage("com.example.fsmwrapper")
                .setScanners(Scanners.MethodsAnnotated, Scanners.TypesAnnotated));

        Set<Class<?>> annotated =
                reflections.get(Scanners.TypesAnnotated.with(StateTransitionValidator.class).asClass());

        Set<Method> methods = reflections.get(Scanners.MethodsAnnotated.with(StateTransitionValidator.class).as(Method.class));
        log.info("allAnnotatedClasses = " + annotated + "" + methods);
    }
}
