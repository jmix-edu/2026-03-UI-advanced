package com.company.timesheets.view.documentacknowledgement;

import com.company.timesheets.entity.DocumentAcknowledgement;
import com.company.timesheets.view.main.MainView;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import io.jmix.core.Messages;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.LookupComponent;
import io.jmix.flowui.view.StandardListView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "my-document-acknowledgements", layout = MainView.class)
@ViewController("ts_DocumentAcknowledgement.my")
@ViewDescriptor("my-document-acknowledgement-list-view.xml")
@LookupComponent("acknowledgementsDataGrid")
@DialogMode(width = "72em")
public class MyDocumentAcknowledgementListView extends StandardListView<DocumentAcknowledgement> {
    // Входящая очередь сотрудника: что ему назначено на ознакомление и в каком это статусе.

    @Autowired
    private Messages messages;

    @io.jmix.flowui.view.Supply(to = "acknowledgementsDataGrid.status", subject = "renderer")
    private Renderer<DocumentAcknowledgement> acknowledgementsDataGridStatusRenderer() {
        return new ComponentRenderer<>(acknowledgement -> new Span(messages.getMessage(acknowledgement.getStatus())));
    }
}
