package com.company.timesheets.listener;

import com.company.timesheets.entity.DocumentAcknowledgement;
import com.company.timesheets.entity.User;
import com.company.timesheets.event.DocumentAcknowledgementChangedEvent;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.Id;
import io.jmix.core.event.AttributeChanges;
import io.jmix.core.event.EntityChangedEvent;
import io.jmix.flowui.UiEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Collections;

@Component("ts_DocumentAcknowledgementEventListener")
public class DocumentAcknowledgementEventListener {

    private final DataManager dataManager;
    private final UiEventPublisher uiEventPublisher;

    public DocumentAcknowledgementEventListener(DataManager dataManager, UiEventPublisher uiEventPublisher) {
        this.dataManager = dataManager;
        this.uiEventPublisher = uiEventPublisher;
    }

    /*
     * Бейдж в MainView должен обновляться только после коммита статуса acknowledgement.
     * Поэтому публикуем UI-событие для конкретного пользователя по образцу TimeEntryEventListener.
     */
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onDocumentAcknowledgementChangedAfterCommit(EntityChangedEvent<DocumentAcknowledgement> event) {
        AttributeChanges changes = event.getChanges();

        if (event.getType() != EntityChangedEvent.Type.DELETED && changes.isChanged("status")) {
            DocumentAcknowledgement acknowledgement = dataManager.load(event.getEntityId())
                    .fetchPlan(fpb -> fpb.add("user", FetchPlan.INSTANCE_NAME))
                    .one();
            publishEvent(acknowledgement.getUser().getUsername());
        }

        if (event.getType() == EntityChangedEvent.Type.DELETED) {
            Id<User> userId = changes.getOldValue("user");
            if (userId != null) {
                User user = dataManager.load(userId).one();
                publishEvent(user.getUsername());
            }
        }
    }

    private void publishEvent(String username) {
        uiEventPublisher.publishEventForUsers(
                new DocumentAcknowledgementChangedEvent(this),
                Collections.singleton(username)
        );
    }
}
