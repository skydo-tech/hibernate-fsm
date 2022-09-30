package com.skydo.lib.fsm.internal;

import com.skydo.lib.fsm.internal.tools.StateMachine;
import org.hibernate.annotations.common.reflection.ReflectionManager;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.cfg.AccessType;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.service.ServiceRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntitiesConfigurator {

    public EntitiesConfigurations configure(
            MetadataImplementor metadata,
            ServiceRegistry serviceRegistry,
            ReflectionManager reflectionManager) {

        Iterable<PersistentClass> entityClassMetadata = metadata.getEntityBindings();
        List<PersistentClass> stateManagedEntities = new ArrayList<>();
        Map<String, List<XProperty>> stateManagedFieldsMap = new HashMap<>();

        for (PersistentClass pc : entityClassMetadata) {
            XClass xClass = reflectionManager.toXClass(pc.getMappedClass());
            List<XProperty> entityStateFields = stateManagedFieldsMap.get(pc.getEntityName());
            if (entityStateFields == null)
                entityStateFields = new ArrayList<>();
            for (XProperty xProperty : xClass.getDeclaredProperties(AccessType.FIELD.getType())) {
                if (xProperty.isAnnotationPresent(StateMachine.class)) {
                    entityStateFields.add(xProperty);
                    stateManagedEntities.add(pc);
                }
            }
            stateManagedFieldsMap.put(pc.getEntityName(), entityStateFields);
        }

        return new EntitiesConfigurations(stateManagedEntities, stateManagedFieldsMap);
    }
}
