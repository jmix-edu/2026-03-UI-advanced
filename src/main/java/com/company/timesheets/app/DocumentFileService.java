package com.company.timesheets.app;

import com.company.timesheets.entity.Document;
import com.company.timesheets.entity.DocumentAttachment;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageException;
import io.jmix.core.FileStorageLocator;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.flowui.upload.TemporaryStorage;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service("ts_DocumentFileService")
public class DocumentFileService {

    private final DataManager dataManager;
    private final TemporaryStorage temporaryStorage;
    private final FileStorageLocator fileStorageLocator;

    public DocumentFileService(DataManager dataManager,
                               TemporaryStorage temporaryStorage,
                               FileStorageLocator fileStorageLocator) {
        this.dataManager = dataManager;
        this.temporaryStorage = temporaryStorage;
        this.fileStorageLocator = fileStorageLocator;
    }

    public FileRef moveToPermanentStorage(UUID temporaryFileId, String fileName) {
        /*
         * TemporaryStorage сам удаляет temp-файл после успешного putFileIntoStorage().
         * Это и есть граница между "черновой загрузкой на экране" и "файл стал частью состояния системы".
         */
        return temporaryStorage.putFileIntoStorage(temporaryFileId, fileName);
    }

    public void deleteTemporaryFile(@Nullable UUID temporaryFileId) {
        if (temporaryFileId == null) {
            return;
        }

        try {
            temporaryStorage.deleteFile(temporaryFileId);
        } catch (FileStorageException e) {
            if (e.getType() != FileStorageException.Type.FILE_NOT_FOUND) {
                throw e;
            }
        }
    }

    public void deleteStoredFile(@Nullable FileRef fileRef) {
        if (fileRef == null) {
            return;
        }

        FileStorage fileStorage = fileStorageLocator.getByName(fileRef.getStorageName());
        try {
            if (fileStorage.fileExists(fileRef)) {
                fileStorage.removeFile(fileRef);
            }
        } catch (FileStorageException e) {
            if (e.getType() != FileStorageException.Type.FILE_NOT_FOUND) {
                throw e;
            }
        }
    }

    public void deleteStoredFileIfUnused(@Nullable FileRef fileRef) {
        if (fileRef == null) {
            return;
        }

        Long usages = dataManager.loadValue(
                        "select count(e) from ts_DocumentAttachment e where e.file = :fileRef",
                        Long.class)
                .parameter("fileRef", fileRef)
                .one();

        if (usages == 0) {
            deleteStoredFile(fileRef);
        }
    }

    public Document createDraftDocumentWithAttachments(String name,
                                                       @Nullable String description,
                                                       NewAttachment... attachments) {
        return createDraftDocumentWithAttachments(name, description, Arrays.asList(attachments));
    }

    public Document createDraftDocumentWithAttachments(String name,
                                                       @Nullable String description,
                                                       List<NewAttachment> attachments) {
        Document document = dataManager.create(Document.class);
        document.setName(name);
        document.setDescription(description);

        SaveContext saveContext = new SaveContext();
        saveContext.saving(document);

        FileStorage fileStorage = fileStorageLocator.getDefault();
        for (NewAttachment attachment : attachments) {
            FileRef fileRef = fileStorage.saveStream(
                    attachment.fileName(),
                    new ByteArrayInputStream(attachment.content())
            );

            DocumentAttachment documentAttachment = dataManager.create(DocumentAttachment.class);
            documentAttachment.setDocument(document);
            documentAttachment.setFileName(attachment.fileName());
            documentAttachment.setFile(fileRef);

            document.getAttachments().add(documentAttachment);
            saveContext.saving(documentAttachment);
        }

        return (Document) dataManager.save(saveContext).get(document);
    }

    public record NewAttachment(String fileName, byte[] content) {
    }
}
