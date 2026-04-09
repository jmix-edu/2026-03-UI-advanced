package com.company.timesheets.view.documentacknowledgement;

import com.company.timesheets.app.DocumentAcknowledgementService;
import com.company.timesheets.entity.DocumentAcknowledgement;
import com.company.timesheets.entity.DocumentAcknowledgementStatus;
import com.company.timesheets.entity.DocumentAttachment;
import com.company.timesheets.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.Messages;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "my-document-acknowledgements/:id", layout = MainView.class)
@ViewController("ts_DocumentAcknowledgement.detail")
@ViewDescriptor("document-acknowledgement-detail-view.xml")
@EditedEntityContainer("acknowledgementDc")
@DialogMode(width = "72em")
public class DocumentAcknowledgementDetailView extends StandardDetailView<DocumentAcknowledgement> {
    /*
     * Экран сотрудника.
     * Здесь документ только читается, а единственное действие пользователя — подтвердить ознакомление подписью.
     */

    @ViewComponent
    private TypedTextField<String> documentNameField;
    @ViewComponent
    private TypedTextField<String> statusField;
    @ViewComponent
    private JmixTextArea documentDescriptionField;
    @ViewComponent
    private DataGrid<DocumentAttachment> attachmentsDataGrid;
    @ViewComponent
    private CollectionContainer<DocumentAttachment> attachmentsDc;
    @ViewComponent
    private JmixButton signBtn;

    @Autowired
    private DocumentAcknowledgementService documentAcknowledgementService;
    @Autowired
    private Notifications notifications;
    @Autowired
    private Downloader downloader;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private Messages messages;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        // Само открытие карточки уже считается фактом просмотра.
        DocumentAcknowledgement acknowledgement = documentAcknowledgementService.markViewedIfNeeded(getEditedEntity().getId());
        setEntityToEdit(acknowledgement);
        refreshFields();
    }

    @Subscribe(id = "signBtn", subject = "clickListener")
    public void onSignBtnClick(final ClickEvent<JmixButton> event) {
        // После подписи повторное действие отключается на этом же экране.
        DocumentAcknowledgement acknowledgement = documentAcknowledgementService.sign(getEditedEntity().getId());
        setEntityToEdit(acknowledgement);
        refreshFields();
        notifications.create(messages.getMessage("com.company.timesheets.view.documentacknowledgement/signCompletedNotification")).show();
    }

    private void refreshFields() {
        // Поля собираются из acknowledgement и связанного опубликованного документа.
        DocumentAcknowledgement acknowledgement = getEditedEntity();
        documentNameField.setValue(acknowledgement.getDocument().getName());
        documentDescriptionField.setValue(acknowledgement.getDocument().getDescription());
        statusField.setValue(messages.getMessage(acknowledgement.getStatus()));
        attachmentsDc.setItems(acknowledgement.getDocument().getAttachments());
        signBtn.setEnabled(acknowledgement.getStatus() != DocumentAcknowledgementStatus.SIGNED);
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
}
