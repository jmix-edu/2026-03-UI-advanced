package com.company.timesheets.event;

import org.springframework.context.ApplicationEvent;

public class DocumentAcknowledgementChangedEvent extends ApplicationEvent {
    // Лёгкое UI-событие: после просмотра или подписи пересчитать бейдж и связанные пользовательские списки.

    public DocumentAcknowledgementChangedEvent(Object source) {
        super(source);
    }
}
