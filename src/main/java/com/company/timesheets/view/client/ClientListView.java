package com.company.timesheets.view.client;

import com.company.timesheets.entity.Client;
import com.company.timesheets.view.main.MainView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "clients", layout = MainView.class)
@ViewController("ts_Client.list")
@ViewDescriptor("client-list-view.xml")
@LookupComponent("clientsDataGrid")
//@PrimaryLookupView(Client.class)
@DialogMode(width = "64em")
public class ClientListView extends StandardListView<Client> {
    @Autowired
    private UiComponents uiComponents;
    @Subscribe
    public void onInit(final InitEvent event) {
        H2 header = uiComponents.create(H2.class);
        header.setText("Clients list INIT");
        getContent().add(header);
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        H2 header = uiComponents.create(H2.class);
        header.setText("Clients list Before Show!");
        getContent().add(header);
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        H2 header = uiComponents.create(H2.class);
        header.setText("Clients list READY!");
        getContent().add(header);
    }

//    @Supply(to = "clientsDataGrid.name", subject = "renderer")
//    private Renderer<Client> clientsDataGridNameRenderer() {
//        return new ComponentRenderer<>(client -> {
//            // TODO: create suitable component
//            Span span = uiComponents.create(Span.class);
//            span.setText(client.toString());
//            return span;
//        });
//    }

}