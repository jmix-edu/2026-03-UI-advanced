package com.company.timesheets.app;

import com.company.timesheets.entity.Document;
import com.company.timesheets.entity.DocumentAcknowledgement;
import com.company.timesheets.entity.DocumentAcknowledgementStatus;
import com.company.timesheets.entity.DocumentStatus;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

@Component("ts_DocumentDemoDataInitializer")
public class DocumentDemoDataInitializer implements ApplicationRunner {
    /*
     * Инициализатор нужен только для живого тренинга:
     * при пустой базе он создаёт опубликованный документ, следующий draft и пример подписанной карточки.
     */

    private static final String DEMO_DOCUMENT_NAME = "Safety Briefing Demo v1";

    private final DataManager dataManager;
    private final DocumentFileService documentFileService;
    private final DocumentDistributionService documentDistributionService;
    private final SystemAuthenticator systemAuthenticator;

    public DocumentDemoDataInitializer(DataManager dataManager,
                                       DocumentFileService documentFileService,
                                       DocumentDistributionService documentDistributionService,
                                       SystemAuthenticator systemAuthenticator) {
        this.dataManager = dataManager;
        this.documentFileService = documentFileService;
        this.documentDistributionService = documentDistributionService;
        this.systemAuthenticator = systemAuthenticator;
    }

    @Override
    public void run(ApplicationArguments args) {
        systemAuthenticator.runWithSystem(this::initializeDemoData);
    }

    protected void initializeDemoData() {
        // Защита от повторного сидинга при каждом рестарте.
        Long existing = dataManager.loadValue(
                        "select count(e) from ts_Document e where e.name = :name",
                        Long.class)
                .parameter("name", DEMO_DOCUMENT_NAME)
                .one();
        if (existing > 0) {
            return;
        }

        Document publishedDocument = documentFileService.createDraftDocumentWithAttachments(
                DEMO_DOCUMENT_NAME,
                "Published demo document for training. This version is locked and distributed to active users.",
                new DocumentFileService.NewAttachment(
                        "safety-introduction.txt",
                        demoText("General safety introduction", "Read the general rules before starting work.")
                ),
                new DocumentFileService.NewAttachment(
                        "ppe-checklist.txt",
                        demoText("PPE checklist", "Helmet, gloves, glasses and briefing confirmation are required.")
                )
        );

        documentDistributionService.publishToAllActiveUsers(publishedDocument.getId());

        Document nextDraft = documentDistributionService.createNextDraftVersion(publishedDocument.getId());
        nextDraft.setName("Safety Briefing Demo v2");
        nextDraft.setDescription("Draft version prepared after publication. Use it to replace files before the next wave.");
        dataManager.save(nextDraft);

        DocumentAcknowledgement acknowledgement = dataManager.load(DocumentAcknowledgement.class)
                .query("select e from ts_DocumentAcknowledgement e where e.document.id = :documentId order by e.sentAt asc")
                .parameter("documentId", publishedDocument.getId())
                .optional()
                .orElse(null);

        if (acknowledgement != null) {
            OffsetDateTime now = OffsetDateTime.now();
            acknowledgement.setStatus(DocumentAcknowledgementStatus.SIGNED);
            acknowledgement.setViewedAt(now.minusMinutes(10));
            acknowledgement.setSignedAt(now.minusMinutes(5));
            acknowledgement.setSignatureInfo("Demo EDS signature");
            dataManager.save(acknowledgement);
        }
    }

    private byte[] demoText(String title, String body) {
        // Простые текстовые вложения удобны для демо: видны в UI и не требуют внешних файлов в репозитории.
        return (title + System.lineSeparator() + System.lineSeparator() + body + System.lineSeparator())
                .getBytes(StandardCharsets.UTF_8);
    }
}
