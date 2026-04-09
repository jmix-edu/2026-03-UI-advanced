package com.company.timesheets.view.document;

import com.company.timesheets.app.DocumentDistributionService;
import com.company.timesheets.app.DocumentFileService;
import com.company.timesheets.entity.Document;
import com.company.timesheets.entity.DocumentAttachment;
import com.company.timesheets.entity.DocumentStatus;
import com.company.timesheets.view.main.MainView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.FileRef;
import io.jmix.core.Messages;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.upload.FileStorageUploadField;
import io.jmix.flowui.download.Downloader;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Route(value = "documents/:id", layout = MainView.class)
@ViewController("ts_Document.detail")
@ViewDescriptor("document-detail-view.xml")
@EditedEntityContainer("documentDc")
@DialogMode(width = "72em")
public class DocumentDetailView extends StandardDetailView<Document> {

    @ViewComponent
    private FileStorageUploadField attachmentUploadField;
    @ViewComponent
    private TypedTextField<String> pendingUploadField;
    @ViewComponent
    private TypedTextField<String> statusField;
    @ViewComponent
    private DataGrid<DocumentAttachment> attachmentsDataGrid;
    @ViewComponent
    private TypedTextField<String> nameField;
    @ViewComponent
    private JmixTextArea descriptionField;
    @ViewComponent
    private JmixButton saveAndCloseBtn;
    @ViewComponent
    private JmixButton publishBtn;
    @ViewComponent
    private JmixButton nextDraftBtn;
    @ViewComponent
    private JmixButton removeAttachmentBtn;
    @ViewComponent
    private JmixButton clearPendingUploadsBtn;
    @ViewComponent
    private MessageBundle messageBundle;

    @Autowired
    private DocumentFileService documentFileService;
    @Autowired
    private DocumentDistributionService documentDistributionService;
    @Autowired
    private Notifications notifications;
    @Autowired
    private Downloader downloader;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private Messages messages;

    private final List<PendingAttachmentUpload> pendingUploads = new ArrayList<>();

    @Subscribe
    public void onReady(final ReadyEvent event) {
        refreshPendingUploadState();
        refreshFormState();
    }

    @Subscribe("attachmentUploadField")
    public void onAttachmentUploadFieldFileUploadSucceeded(
            final FileUploadSucceededEvent<FileStorageUploadField> event) {
        FileTemporaryStorageBuffer receiver = event.getReceiver();
        pendingUploads.add(new PendingAttachmentUpload(
                receiver.getFileData().getFileInfo().getId(),
                event.getFileName()
        ));
        refreshPendingUploadState();
    }

    @Subscribe(id = "removeAttachmentBtn", subject = "clickListener")
    public void onRemoveAttachmentBtnClick(final ClickEvent<JmixButton> event) {
        if (getEditedEntity().getStatus() != DocumentStatus.DRAFT) {
            notifications.create(messageBundle.getMessage("documentLockedNotification")).show();
            return;
        }

        DocumentAttachment selectedAttachment = attachmentsDataGrid.getSingleSelectedItem();
        if (selectedAttachment == null) {
            notifications.create(messageBundle.getMessage("attachmentSelectionRequired")).show();
            return;
        }

        getEditedEntity().getAttachments().remove(selectedAttachment);
        getViewData().getDataContext().remove(selectedAttachment);
    }

    @Subscribe(id = "clearPendingUploadsBtn", subject = "clickListener")
    public void onClearPendingUploadsBtnClick(final ClickEvent<JmixButton> event) {
        clearPendingUploads();
        refreshPendingUploadState();
    }

    @Subscribe(id = "publishBtn", subject = "clickListener")
    public void onPublishBtnClick(final ClickEvent<JmixButton> event) {
        if (getEditedEntity().getId() == null || getViewData().getDataContext().hasChanges() || hasPendingUploads()) {
            notifications.create(messageBundle.getMessage("publishRequiresSavedDocument")).show();
            return;
        }

        if (getEditedEntity().getStatus() != DocumentStatus.DRAFT) {
            notifications.create(messageBundle.getMessage("documentAlreadyPublished")).show();
            return;
        }

        int assignmentsCreated = documentDistributionService.publishToAllActiveUsers(getEditedEntity().getId());
        getViewData().loadAll();
        notifications.create(messageBundle.formatMessage("documentPublishedNotification", assignmentsCreated)).show();
        refreshFormState();
    }

    @Subscribe(id = "nextDraftBtn", subject = "clickListener")
    public void onNextDraftBtnClick(final ClickEvent<JmixButton> event) {
        if (getEditedEntity().getId() == null || getViewData().getDataContext().hasChanges() || hasPendingUploads()) {
            notifications.create(messageBundle.getMessage("nextDraftRequiresSavedDocument")).show();
            return;
        }

        if (getEditedEntity().getStatus() == DocumentStatus.DRAFT) {
            notifications.create(messageBundle.getMessage("nextDraftFromPublishedOnly")).show();
            return;
        }

        Document nextDraft = documentDistributionService.createNextDraftVersion(getEditedEntity().getId());
        notifications.create(messageBundle.formatMessage("nextDraftCreatedNotification", nextDraft.getName())).show();
        UI.getCurrent().navigate("documents/" + nextDraft.getId());
    }

    @Subscribe
    public void onBeforeSave(final BeforeSaveEvent event) {
        if (getEditedEntity().getStatus() != DocumentStatus.DRAFT) {
            event.preventSave();
            notifications.create(messageBundle.getMessage("documentLockedNotification")).show();
            return;
        }

        if (!hasPendingUploads()) {
            return;
        }

        event.preventSave();

        List<FileRef> uploadedFiles = new ArrayList<>();
        List<DocumentAttachment> createdAttachments = new ArrayList<>();

        try {
            for (PendingAttachmentUpload pendingUpload : pendingUploads) {
                FileRef uploadedFile = documentFileService.moveToPermanentStorage(
                        pendingUpload.temporaryFileId(),
                        pendingUpload.fileName()
                );
                uploadedFiles.add(uploadedFile);

                DocumentAttachment attachment = getViewData().getDataContext().create(DocumentAttachment.class);
                attachment.setDocument(getEditedEntity());
                attachment.setFileName(pendingUpload.fileName());
                attachment.setFile(uploadedFile);

                getEditedEntity().getAttachments().add(attachment);
                createdAttachments.add(attachment);
            }

            pendingUploads.clear();
            refreshPendingUploadState();
            event.resume();
        } catch (RuntimeException e) {
            for (DocumentAttachment attachment : createdAttachments) {
                getEditedEntity().getAttachments().remove(attachment);
                getViewData().getDataContext().remove(attachment);
            }
            for (FileRef uploadedFile : uploadedFiles) {
                documentFileService.deleteStoredFile(uploadedFile);
            }
            throw e;
        }
    }

    @Subscribe
    public void onBeforeClose(final BeforeCloseEvent event) {
        clearPendingUploads();
        refreshPendingUploadState();
    }

    private boolean hasPendingUploads() {
        return !pendingUploads.isEmpty();
    }

    private void clearPendingUploads() {
        for (PendingAttachmentUpload pendingUpload : pendingUploads) {
            documentFileService.deleteTemporaryFile(pendingUpload.temporaryFileId());
        }
        pendingUploads.clear();
    }

    private void refreshPendingUploadState() {
        boolean pendingUpload = hasPendingUploads();
        pendingUploadField.setVisible(pendingUpload);
        pendingUploadField.setValue(pendingUpload
                ? pendingUploads.stream().map(PendingAttachmentUpload::fileName).collect(Collectors.joining(", "))
                : messageBundle.getMessage("pendingUploadField.empty"));
    }

    private void refreshFormState() {
        boolean draft = getEditedEntity().getStatus() == DocumentStatus.DRAFT;
        statusField.setValue(getEditedEntity().getStatus() == null
                ? null
                : messages.getMessage(getEditedEntity().getStatus()));
        nameField.setReadOnly(!draft);
        descriptionField.setReadOnly(!draft);
        attachmentUploadField.setEnabled(draft);
        saveAndCloseBtn.setEnabled(draft);
        publishBtn.setEnabled(draft);
        nextDraftBtn.setEnabled(!draft);
        removeAttachmentBtn.setEnabled(draft);
        clearPendingUploadsBtn.setEnabled(draft);
    }

    private record PendingAttachmentUpload(UUID temporaryFileId, String fileName) {
    }

    @io.jmix.flowui.view.Supply(to = "attachmentsDataGrid.fileName", subject = "renderer")
    private Renderer<DocumentAttachment> attachmentsDataGridFileNameRenderer() {
        return new ComponentRenderer<>(attachment -> {
            Button button = uiComponents.create(Button.class);
            button.setText(attachment.getFileName());
            button.addThemeNames("tertiary-inline");
            button.addClickListener(event -> downloader.download(attachment.getFile()));
            return button;
        });
    }

    @io.jmix.flowui.view.Supply(to = "acknowledgementsDataGrid.status", subject = "renderer")
    private Renderer<com.company.timesheets.entity.DocumentAcknowledgement> acknowledgementsDataGridStatusRenderer() {
        return new ComponentRenderer<>(acknowledgement -> new Span(messages.getMessage(acknowledgement.getStatus())));
    }
}
