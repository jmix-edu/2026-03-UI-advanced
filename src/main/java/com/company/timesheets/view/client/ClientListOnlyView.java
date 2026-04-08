package com.company.timesheets.view.client;

import com.company.timesheets.entity.Client;
import com.company.timesheets.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "clients-list-only", layout = MainView.class)
@ViewController(id = "ts_Client_only.list")
@ViewDescriptor(path = "client-list-only-view.xml")
@LookupComponent("clientsDataGrid")
@DialogMode(width = "64em")
public class ClientListOnlyView extends StandardListView<Client> {
}