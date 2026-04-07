package com.company.timesheets.view.document;

import com.company.timesheets.app.DocumentFileService;
import com.company.timesheets.entity.Document;
import com.company.timesheets.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.FileRef;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.upload.FileStorageUploadField;
import io.jmix.flowui.component.upload.receiver.FileTemporaryStorageBuffer;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.upload.event.FileUploadSucceededEvent;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Route(value = "documents/:id", layout = MainView.class)
@ViewController("ts_Document.detail")
@ViewDescriptor("document-detail-view.xml")
@EditedEntityContainer("documentDc")
@DialogMode(width = "48em")
public class DocumentDetailView extends StandardDetailView<Document> {

    @ViewComponent
    private FileStorageUploadField attachmentField;
    @ViewComponent
    private TypedTextField<String> pendingUploadField;
    @ViewComponent
    private MessageBundle messageBundle;

    @Autowired
    private DocumentFileService documentFileService;

    private UUID pendingTemporaryFileId;
    private String pendingFileName;

    /*
     * Экран хранит "кандидат на сохранение" отдельно от сущности.
     *
     * Почему так:
     * 1. Пользователь может несколько раз заменить файл, не сохраняя сущность.
     * 2. Пользователь может вообще закрыть экран без сохранения.
     * 3. Если писать FileRef в сущность сразу при upload, в постоянном storage останутся сироты.
     *
     * Поэтому до save() держим только идентификатор временного файла.
     * В саму сущность attachment попадает только файл, уже перенесённый в постоянное storage.
     */
    @Subscribe
    public void onReady(final ReadyEvent event) {
        refreshPendingUploadState();
    }

    @Subscribe("attachmentField")
    public void onAttachmentFieldFileUploadSucceeded1(final FileUploadSucceededEvent<FileStorageUploadField> event) {
        /*
         * При повторной загрузке нового файла пользователь фактически "передумал"
         * насчёт предыдущего незасейвленного варианта.
         *
         * Поэтому старый temp-файл нужно удалить сразу, иначе он станет мусором.
         */
        clearPendingUpload();

        FileTemporaryStorageBuffer receiver = event.getReceiver();
        pendingTemporaryFileId = receiver.getFileData().getFileInfo().getId();
        pendingFileName = event.getFileName();

        refreshPendingUploadState();
    }

    @Subscribe(id = "clearAttachmentBtn", subject = "clickListener")
    public void onClearAttachmentBtnClick(final ClickEvent<JmixButton> event) {
        if (hasPendingUpload()) {
            /*
             * Если на экране уже есть новый temp-файл, удаляем именно его.
             * Сущность при этом откатываем к уже сохранённому attachment.
             */
            clearPendingUpload();
            attachmentField.setValue(getEditedEntity().getAttachment());
            refreshPendingUploadState();
            return;
        }

        /*
         * Если pending upload нет, пользователь очищает текущее сохранённое вложение.
         * Сам физический файл будет удалён не здесь, а после успешного коммита сущности
         * через EntityChangedEvent listener.
         */
        getEditedEntity().setAttachment(null);
        attachmentField.clear();
        refreshPendingUploadState();
    }

    @Subscribe
    public void onBeforeSave(final BeforeSaveEvent event) {
        if (!hasPendingUpload()) {
            return;
        }

        /*
         * Здесь выполняется вторая фаза сценария:
         * 1. temp-файл переносим в постоянное storage;
         * 2. только потом присваиваем FileRef сущности;
         * 3. затем возобновляем стандартный save экрана.
         *
         * Таким образом, отмена экрана не создаёт мусор в постоянном storage,
         * а успешное сохранение всегда оперирует уже валидным FileRef.
         */
        event.preventSave();

        FileRef previousAttachment = getEditedEntity().getAttachment();
        FileRef uploadedAttachment = null;

        try {
            uploadedAttachment = documentFileService.moveToPermanentStorage(pendingTemporaryFileId, pendingFileName);
            getEditedEntity().setAttachment(uploadedAttachment);
            attachmentField.setValue(uploadedAttachment);
            resetPendingUploadState();

            event.resume();
        } catch (RuntimeException e) {
            /*
             * Если перенос в storage или дальнейшее сохранение падает,
             * возвращаем экран в предыдущее состояние.
             * Если новый файл уже успели положить в storage, удаляем его сразу.
             */
            getEditedEntity().setAttachment(previousAttachment);
            attachmentField.setValue(previousAttachment);

            if (uploadedAttachment != null) {
                documentFileService.deleteStoredFile(uploadedAttachment);
            }
            throw e;
        } finally {
            refreshPendingUploadState();
        }
    }

    @Subscribe
    public void onBeforeClose(final BeforeCloseEvent event) {
        /*
         * На закрытии экрана всегда чистим незасейвленный temp-файл.
         * Это покрывает cancel, закрытие диалога и прочие сценарии выхода без save.
         */
        clearPendingUpload();
        refreshPendingUploadState();
    }

    private boolean hasPendingUpload() {
        return pendingTemporaryFileId != null;
    }

    private void clearPendingUpload() {
        documentFileService.deleteTemporaryFile(pendingTemporaryFileId);
        resetPendingUploadState();
    }

    private void resetPendingUploadState() {
        pendingTemporaryFileId = null;
        pendingFileName = null;
    }

    private void refreshPendingUploadState() {
        boolean pendingUpload = hasPendingUpload();
        pendingUploadField.setVisible(pendingUpload);
        pendingUploadField.setValue(pendingUpload
                ? pendingFileName
                : messageBundle.getMessage("pendingUploadField.empty"));
    }
}
