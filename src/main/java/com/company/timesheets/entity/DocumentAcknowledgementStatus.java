package com.company.timesheets.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum DocumentAcknowledgementStatus implements EnumClass<String> {

    // Документ назначен сотруднику, но карточку ещё не открывали.
    NEW("new"),
    // Карточка уже открывалась, но подпись ещё не поставлена.
    VIEWED("viewed"),
    // Ознакомление завершено и больше не требует действий.
    SIGNED("signed");

    private final String id;

    DocumentAcknowledgementStatus(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static DocumentAcknowledgementStatus fromId(String id) {
        for (DocumentAcknowledgementStatus value : DocumentAcknowledgementStatus.values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }
}
