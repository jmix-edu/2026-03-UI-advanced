package com.company.timesheets.app;

import com.company.timesheets.entity.Document;
import io.jmix.core.FileRef;
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
    public void onDocumentChanged(EntityChangedEvent<Document> event) {
        /*
         * Cleanup постоянного storage делаем только после коммита.
         *
         * Почему не раньше:
         * 1. При rollback нельзя терять старый файл.
         * 2. До коммита ещё неясно, действительно ли замена/удаление состоялись.
         *
         * Поэтому здесь удаляем только "старый" уже сохранённый файл:
         * - при замене attachment;
         * - при удалении самой сущности.
         */
        if (event.getType() == EntityChangedEvent.Type.UPDATED && event.getChanges().isChanged("attachment")) {
            documentFileService.deleteStoredFile(event.getChanges().getOldValue("attachment"));
        }

        if (event.getType() == EntityChangedEvent.Type.DELETED) {
            documentFileService.deleteStoredFile(event.getChanges().getOldValue("attachment"));
        }
    }
}
