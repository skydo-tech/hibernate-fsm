package com.skydo.lib.fsm.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;


public class EnableStateMachinePackages {

    private static final String BEAN = EnableStateMachinePackages.class.getName();

    private static final EnableStateMachinePackages NONE = new EnableStateMachinePackages();

    private final List<String> packageNames;

    EnableStateMachinePackages(String... packageNames) {
        List<String> packages = new ArrayList<>();
        for (String name : packageNames) {
            if (StringUtils.hasText(name)) {
                packages.add(name);
            }
        }
        this.packageNames = Collections.unmodifiableList(packages);
    }


    public static EnableStateMachinePackages get(BeanFactory beanFactory) {
        // Currently we only store a single base package, but we return a list to
        // allow this to change in the future if needed
        try {
            return beanFactory.getBean(BEAN, EnableStateMachinePackages.class);
        }
        catch (NoSuchBeanDefinitionException ex) {
            return NONE;
        }
    }


    public static void register(BeanDefinitionRegistry registry, String... packageNames) {
        Assert.notNull(registry, "Registry must not be null");
        Assert.notNull(packageNames, "PackageNames must not be null");
        register(registry, Arrays.asList(packageNames));
    }


    public static void register(BeanDefinitionRegistry registry, Collection<String> packageNames) {
        Assert.notNull(registry, "Registry must not be null");
        Assert.notNull(packageNames, "PackageNames must not be null");
        if (registry.containsBeanDefinition(BEAN)) {
            EntityScanPackagesBeanDefinition beanDefinition = (EntityScanPackagesBeanDefinition) registry
                    .getBeanDefinition(BEAN);
            beanDefinition.addPackageNames(packageNames);
        }
        else {
            registry.registerBeanDefinition(BEAN, new EntityScanPackagesBeanDefinition(packageNames));
        }
    }


    static class Registrar implements ImportBeanDefinitionRegistrar {

        private Logger log = LoggerFactory.getLogger(Registrar.class);

        private final Environment environment;

        Registrar(Environment environment) {
            this.environment = environment;
        }

        static public Set<String> stateMachinePackageScanConfig;

        public Set<String> getStateMachinePackageScanConfig() {
            return stateMachinePackageScanConfig;
        }

        @Override
        public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {

            stateMachinePackageScanConfig = getPackagesToScan(metadata);

            log.info(stateMachinePackageScanConfig.toString());

//            register(registry, getPackagesToScan(metadata));
        }

        private Set<String> getPackagesToScan(AnnotationMetadata metadata) {
            AnnotationAttributes attributes = AnnotationAttributes
                    .fromMap(metadata.getAnnotationAttributes(EnableStateMachine.class.getName()));
            Set<String> packagesToScan = new LinkedHashSet<>();
            for (String basePackage : attributes.getStringArray("basePackages")) {
                String[] tokenized = StringUtils.tokenizeToStringArray(
                        this.environment.resolvePlaceholders(basePackage),
                        ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
                Collections.addAll(packagesToScan, tokenized);
            }
            for (Class<?> basePackageClass : attributes.getClassArray("basePackageClasses")) {
                packagesToScan.add(this.environment.resolvePlaceholders(ClassUtils.getPackageName(basePackageClass)));
            }
            if (packagesToScan.isEmpty()) {
                String packageName = ClassUtils.getPackageName(metadata.getClassName());
                Assert.state(StringUtils.hasLength(packageName), "@EntityScan cannot be used with the default package");
                return Collections.singleton(packageName);
            }
            return packagesToScan;
        }

    }

    static class EntityScanPackagesBeanDefinition extends GenericBeanDefinition {

        private final Set<String> packageNames = new LinkedHashSet<>();

        EntityScanPackagesBeanDefinition(Collection<String> packageNames) {
            setBeanClass(EnableStateMachinePackages.class);
            setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            addPackageNames(packageNames);
        }

        @Override
        public Supplier<?> getInstanceSupplier() {
            return () -> new EnableStateMachinePackages(StringUtils.toStringArray(this.packageNames));
        }

        private void addPackageNames(Collection<String> additionalPackageNames) {
            this.packageNames.addAll(additionalPackageNames);
        }

    }

}
