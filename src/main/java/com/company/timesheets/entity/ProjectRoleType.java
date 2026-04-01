package com.company.timesheets.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum ProjectRoleType implements EnumClass<String> {

    MANAGER("manager"),
    APPROVER("approver"),
    MEMBER("member"),
    OBSERVER("observer");

    private final String id;

    ProjectRoleType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static ProjectRoleType fromId(String id) {
        for (ProjectRoleType at : ProjectRoleType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}