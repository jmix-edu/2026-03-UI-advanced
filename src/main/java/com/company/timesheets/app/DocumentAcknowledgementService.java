package com.company.timesheets.app;

import com.company.timesheets.entity.DocumentAcknowledgement;
import com.company.timesheets.entity.DocumentAcknowledgementStatus;
import io.jmix.core.DataManager;
import io.jmix.core.security.CurrentAuthentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service("ts_DocumentAcknowledgementService")
public class DocumentAcknowledgementService {
    /*
     * Сервис обслуживает пользовательский сценарий ознакомления:
     * просмотр назначенной карточки, подписание и подсчёт входящих задач для бейджа.
     */

    private final DataManager dataManager;
    private final CurrentAuthentication currentAuthentication;

    public DocumentAcknowledgementService(DataManager dataManager,
                                          CurrentAuthentication currentAuthentication) {
        this.dataManager = dataManager;
        this.currentAuthentication = currentAuthentication;
    }

    @Transactional
    public DocumentAcknowledgement markViewedIfNeeded(UUID acknowledgementId) {
        // Первый вход в карточку переводит её из NEW в VIEWED, чтобы отделить "назначено" от "прочитано".
        DocumentAcknowledgement acknowledgement = loadOwnAcknowledgement(acknowledgementId);
        if (acknowledgement.getStatus() == DocumentAcknowledgementStatus.NEW) {
            acknowledgement.setStatus(DocumentAcknowledgementStatus.VIEWED);
            acknowledgement.setViewedAt(OffsetDateTime.now());
            acknowledgement = dataManager.save(acknowledgement);
        }
        return acknowledgement;
    }

    @Transactional
    public DocumentAcknowledgement sign(UUID acknowledgementId) {
        // Подпись завершает карточку и публикует событие для мгновенного обновления бейджа в главном меню.
        DocumentAcknowledgement acknowledgement = loadOwnAcknowledgement(acknowledgementId);
        if (acknowledgement.getStatus() != DocumentAcknowledgementStatus.SIGNED) {
            OffsetDateTime now = OffsetDateTime.now();
            if (acknowledgement.getViewedAt() == null) {
                acknowledgement.setViewedAt(now);
            }
            acknowledgement.setStatus(DocumentAcknowledgementStatus.SIGNED);
            acknowledgement.setSignedAt(now);
            acknowledgement.setSignatureInfo("Signed in UI");
            acknowledgement = dataManager.save(acknowledgement);
        }
        return acknowledgement;
    }

    public DocumentAcknowledgement loadOwnAcknowledgement(UUID acknowledgementId) {
        // Загружаем карточку вместе с документом и вложениями, а затем жёстко проверяем владельца записи.
        DocumentAcknowledgement acknowledgement = dataManager.load(DocumentAcknowledgement.class)
                .id(acknowledgementId)
                .fetchPlan(builder -> builder
                        .addFetchPlan("_base")
                        .add("user", user -> user.addFetchPlan("_base"))
                        .add("document", document -> document
                                .addFetchPlan("_base")
                                .add("attachments", attachment -> attachment.addFetchPlan("_base"))))
                .one();

        String currentUsername = currentAuthentication.getUser().getUsername();
        if (!acknowledgement.getUser().getUsername().equals(currentUsername)) {
            throw new IllegalStateException("Acknowledgement is not assigned to current user");
        }
        return acknowledgement;
    }

    public long countPendingForCurrentUser() {
        // В бейдж попадают все карточки текущего пользователя, которые ещё не доведены до SIGNED.
        String currentUsername = currentAuthentication.getUser().getUsername();
        return dataManager.loadValue(
                        "select count(e) from ts_DocumentAcknowledgement e " +
                                "where e.user.username = :username " +
                                "and e.status <> @enum(com.company.timesheets.entity.DocumentAcknowledgementStatus.SIGNED)",
                        Long.class)
                .parameter("username", currentUsername)
                .one();
    }
}
