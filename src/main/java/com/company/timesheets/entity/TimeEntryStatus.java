package com.company.timesheets.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum TimeEntryStatus implements EnumClass<String> {

    NEW("new"),
    APPROVED("approved"),
    REJECTED("rejected"),
    CLOSED("closed");

    private final String id;

    TimeEntryStatus(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static TimeEntryStatus fromId(String id) {
        for (TimeEntryStatus at : TimeEntryStatus.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}