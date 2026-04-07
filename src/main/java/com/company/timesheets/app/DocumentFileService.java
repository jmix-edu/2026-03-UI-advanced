package com.company.timesheets.app;

import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageException;
import io.jmix.core.FileStorageLocator;
import io.jmix.flowui.upload.TemporaryStorage;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("ts_DocumentFileService")
public class DocumentFileService {

    private final TemporaryStorage temporaryStorage;
    private final FileStorageLocator fileStorageLocator;

    public DocumentFileService(TemporaryStorage temporaryStorage, FileStorageLocator fileStorageLocator) {
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
}
