package com.company.timesheets.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum DocumentStatus implements EnumClass<String> {

    // Редактируемый черновик автора.
    DRAFT("draft"),
    // Опубликованный документ, по которому уже созданы персональные ознакомления.
    PUBLISHED("published"),
    // Резервный статус для последующего архивирования старых версий.
    ARCHIVED("archived");

    private final String id;

    DocumentStatus(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static DocumentStatus fromId(String id) {
        for (DocumentStatus value : DocumentStatus.values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }
}
