package com.skydo.lib.fsm.servicecontributor;

import com.skydo.lib.fsm.definitions.StateMachineHandler;
import com.skydo.lib.fsm.definitions.StateMachineHandlerMethod;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.service.spi.Configurable;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class FSMServiceImpl implements FSMService, Configurable {

    private final static Logger log = Logger.getLogger(com.skydo.lib.fsm.internal.FSMServiceImpl.class.getSimpleName());

    private boolean initialized = false;

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void initialize(MetadataImplementor metadata) {
        log.info("Initialize called");

        // create map

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackage("com.skydo.hibernateevents")
                .setScanners(Scanners.MethodsAnnotated, Scanners.TypesAnnotated));

        Set<Class<?>> annotated =
                reflections.get(Scanners.TypesAnnotated.with(StateMachineHandler.class).asClass());

        Set<Method> methods = reflections.get(Scanners.MethodsAnnotated.with(StateMachineHandlerMethod.class).as(Method.class));

        log.info("allAnnotatedClasses = " + annotated + "" + methods);

        initialized = true;
    }

    @Override
    public void configure(Map map) {
        log.info("Configure called");
    }
}
