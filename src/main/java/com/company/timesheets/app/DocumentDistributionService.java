package com.company.timesheets.app;

import com.company.timesheets.entity.Document;
import com.company.timesheets.entity.DocumentAcknowledgement;
import com.company.timesheets.entity.DocumentAcknowledgementStatus;
import com.company.timesheets.entity.DocumentAttachment;
import com.company.timesheets.entity.DocumentStatus;
import com.company.timesheets.entity.User;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("ts_DocumentDistributionService")
public class DocumentDistributionService {
    /*
     * Здесь живёт бизнес-логика публикации документа и выпуска следующего draft.
     * UI только вызывает эти операции, а не собирает их вручную.
     */

    private final DataManager dataManager;

    public DocumentDistributionService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Transactional
    public int publishToAllActiveUsers(UUID documentId) {
        // Публикация не клонирует файлы на каждого сотрудника, а создаёт персональные acknowledgement-записи.
        Document document = dataManager.load(Document.class)
                .id(documentId)
                .fetchPlan(builder -> builder
                        .addFetchPlan("_base")
                        .add("attachments", attachment -> attachment.addFetchPlan("_base"))
                        .add("acknowledgements", acknowledgement -> acknowledgement.add("user", user -> user.addFetchPlan("_base"))))
                .one();

        if (document.getStatus() != DocumentStatus.DRAFT) {
            throw new IllegalStateException("Only draft documents can be published");
        }
        if (document.getAttachments() == null || document.getAttachments().isEmpty()) {
            throw new IllegalStateException("Document must contain at least one attachment before publication");
        }

        List<User> activeUsers = dataManager.load(User.class)
                .query("select e from ts_User e where e.active = true")
                .list();

        Set<UUID> existingUserIds = document.getAcknowledgements().stream()
                .map(ack -> ack.getUser().getId())
                .collect(Collectors.toSet());

        OffsetDateTime now = OffsetDateTime.now();
        SaveContext saveContext = new SaveContext();

        int createdCount = 0;
        for (User user : activeUsers) {
            if (existingUserIds.contains(user.getId())) {
                continue;
            }

            DocumentAcknowledgement acknowledgement = dataManager.create(DocumentAcknowledgement.class);
            acknowledgement.setDocument(document);
            acknowledgement.setUser(user);
            acknowledgement.setStatus(DocumentAcknowledgementStatus.NEW);
            acknowledgement.setSentAt(now);

            document.getAcknowledgements().add(acknowledgement);
            saveContext.saving(acknowledgement);
            createdCount++;
        }

        document.setStatus(DocumentStatus.PUBLISHED);
        saveContext.saving(document);
        dataManager.save(saveContext);

        return createdCount;
    }

    @Transactional
    public Document createNextDraftVersion(UUID documentId) {
        // Следующая версия копирует метаданные и ссылки на текущие вложения, пока автор не заменит файлы.
        Document source = dataManager.load(Document.class)
                .id(documentId)
                .fetchPlan(builder -> builder
                        .addFetchPlan("_base")
                        .add("attachments", attachment -> attachment.addFetchPlan("_base")))
                .one();

        if (source.getStatus() == DocumentStatus.DRAFT) {
            throw new IllegalStateException("Draft document already exists");
        }

        Document draft = dataManager.create(Document.class);
        draft.setName(source.getName());
        draft.setDescription(source.getDescription());
        draft.setStatus(DocumentStatus.DRAFT);

        SaveContext saveContext = new SaveContext();
        saveContext.saving(draft);

        for (DocumentAttachment sourceAttachment : source.getAttachments()) {
            DocumentAttachment copiedAttachment = dataManager.create(DocumentAttachment.class);
            copiedAttachment.setDocument(draft);
            copiedAttachment.setFileName(sourceAttachment.getFileName());
            copiedAttachment.setFile(sourceAttachment.getFile());

            draft.getAttachments().add(copiedAttachment);
            saveContext.saving(copiedAttachment);
        }

        return (Document) dataManager.save(saveContext).get(draft);
    }
}
