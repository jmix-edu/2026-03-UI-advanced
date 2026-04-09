package com.company.timesheets.app;

import com.company.timesheets.entity.DocumentAttachment;
import io.jmix.core.event.EntityChangedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component("ts_DocumentFileCleanup")
public class DocumentFileCleanup {

    private final DocumentFileService documentFileService;

    public DocumentFileCleanup(DocumentFileService documentFileService) {
        this.documentFileService = documentFileService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDocumentAttachmentChanged(EntityChangedEvent<DocumentAttachment> event) {
        /*
         * Физический файл удаляем только после коммита удаления attachment-сущности.
         * Пока пользователь редактирует черновик, отмена экрана не должна затронуть storage.
         */
        if (event.getType() == EntityChangedEvent.Type.DELETED) {
            documentFileService.deleteStoredFileIfUnused(event.getChanges().getOldValue("file"));
        }
    }
}
