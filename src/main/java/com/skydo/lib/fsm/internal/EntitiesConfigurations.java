package com.skydo.lib.fsm.internal;

import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.mapping.PersistentClass;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntitiesConfigurations {

    private Map<String, PersistentClass> stateManagedEntities;

    private Map<String, List<XProperty>> stateManagedFields;


    public EntitiesConfigurations(
            List<PersistentClass> stateManagedEntities,
            Map<String, List<XProperty>> stateManagedFields
    ) {
        this.stateManagedEntities = new HashMap<>();
        for (PersistentClass pc : stateManagedEntities) {
            this.stateManagedEntities.put(pc.getEntityName(), pc);
        }
        this.stateManagedFields = stateManagedFields;
    }

    public boolean hasEntities() {
        return this.stateManagedEntities.size() != 0;
    }

    public PersistentClass get(String entityName) {
        return this.stateManagedEntities.get(entityName);
    }

    public XProperty get(String entityName, String propertyName) {
        List<XProperty> fields = stateManagedFields.get(entityName);
        if (fields == null)
             return null;
        return fields.stream().filter((s) -> s.getName().equals(propertyName)).findFirst().orElse(null);
    }

    public boolean isStateManaged(String entityName, String propertyName) {
        List<XProperty> fields = stateManagedFields.get(entityName);
        return fields != null && fields.stream().anyMatch((s) -> s.getName().equals(propertyName));
    }

    public boolean isStateManaged(String entityName) {
        return get(entityName) != null;
    }
}
