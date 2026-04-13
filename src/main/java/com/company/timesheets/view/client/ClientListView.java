package com.company.timesheets.view.client;

import com.company.timesheets.entity.Client;
import com.company.timesheets.view.main.MainView;
import com.google.common.base.Strings;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.dom.ElementFactory;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.jmix.core.AccessManager;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.accesscontext.InMemoryCrudEntityContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.accesscontext.UiEntityContext;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.util.RemoveOperation;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Route(value = "clients", layout = MainView.class)
@ViewController("ts_Client.list")
@ViewDescriptor("client-list-view.xml")
//@LookupComponent("clientsDataGrid")  - list only view
//@PrimaryLookupView(Client.class)
@DialogMode(width = "64em")
public class ClientListView extends StandardListView<Client> {
    @Autowired
    private UiComponents uiComponents;
    @ViewComponent
    private JmixButton createBtn;

    @ViewComponent
    private CollectionContainer<Client> clientsDc;

    UiEntityContext uiEntityContext;
    InMemoryCrudEntityContext crudEntityContext;

    @Autowired
    private AccessManager accessManager;
    @Autowired
    private ViewNavigators viewNavigators;
    @Autowired
    private MessageTools messageTools;
    @Autowired
    private Messages messages;
    @Autowired
    private RemoveOperation removeOperation;

    @Subscribe
    public void onInit(final InitEvent event) {
        createBtn.setEnabled(getUiEntityContext().isCreatePermitted());
    }

    @Subscribe(id = "createBtn", subject = "clickListener")
    public void onCreateBtnClick(final ClickEvent<JmixButton> event) {
        viewNavigators.detailView(this, Client.class)
                .newEntity()
                .withBackwardNavigation(true)
                .navigate();
    }

    @Supply(to = "clientsList", subject = "renderer")
    private Renderer<Client> clientsListRenderer() {
        return new ComponentRenderer<>(client -> {
            HorizontalLayout card = uiComponents.create(HorizontalLayout.class);
            card.setPadding(true);
            card.addClassNames(
                    LumoUtility.BorderRadius.MEDIUM,
                    LumoUtility.Margin.SMALL,
                    LumoUtility.BoxShadow.SMALL
            );

            card.add(
                    createAvatar(client.getName(), client.getImage(), "var(--lumo-size-xl)"),
                    createInfoLayout(client),
                    createActionsLayout(client)
            );

            return card;
        });
    }

    private Avatar createAvatar(String name, byte[] data, String size) {
        Avatar avatar = uiComponents.create(Avatar.class);
        avatar.setName(name);

        if (data != null) {
            DownloadHandler handler = DownloadHandler.fromInputStream(event ->
                    new DownloadResponse(
                            new ByteArrayInputStream(data),
                            "avatar.img",
                            "application/octet-stream",
                            data.length
                    ));
            avatar.setImageHandler(handler);
        }

        avatar.setWidth(size);
        avatar.setHeight(size);

        return avatar;
    }

    private Component createInfoLayout(Client client) {
        VerticalLayout infoBox = uiComponents.create(VerticalLayout.class);
        infoBox.setPadding(false);

        infoBox.getElement().appendChild(
                ElementFactory.createStrong(client.getName())
        );

        if (client.getContactInformation() != null) {
            infoBox.add(createContactInformationLayout(client));
        }

        return infoBox;
    }

    private Component createContactInformationLayout(Client client) {
        VerticalLayout contactInformationLayout = uiComponents.create(VerticalLayout.class);
        contactInformationLayout.setClassName("contact-information-layout");

        createContactInformationItem(
                client.getContactInformation().getEmail(),
                VaadinIcon.ENVELOPE.create(),
                Text::new
        ).ifPresent(contactInformationLayout::add);

        createContactInformationItem(
                client.getContactInformation().getPhone(),
                VaadinIcon.PHONE.create(),
                Text::new
        ).ifPresent(contactInformationLayout::add);

        createContactInformationItem(
                client.getContactInformation().getAddress(),
                VaadinIcon.MAP_MARKER.create(),
                Text::new
        ).ifPresent(contactInformationLayout::add);

        createContactInformationItem(
                client.getContactInformation().getUrl(),
                VaadinIcon.LINK.create(),
                text -> new Anchor(text, text, AnchorTarget.BLANK)
        ).ifPresent(contactInformationLayout::add);

        Details contactDetails = uiComponents.create(Details.class);
        contactDetails.setOpened(true);
        contactDetails.setSummaryText(
                messageTools.getPropertyCaption(clientsDc.getEntityMetaClass(), "contactInformation"));
        contactDetails.add(contactInformationLayout);

        return contactDetails;
    }

    private Optional<Component> createContactInformationItem(@Nullable String value, Icon icon,
                                                             Function<String, Component> valueRenderer) {
        return Strings.isNullOrEmpty(value) ?
                Optional.empty() :
                Optional.of(new HorizontalLayout(icon, valueRenderer.apply(value)));
    }

    private Component createActionsLayout(Client client) {
        VerticalLayout actionsBox = uiComponents.create(VerticalLayout.class);
        actionsBox.setPadding(false);
        actionsBox.setWidth("auto");

        Button editBtn = createEditButton(client);
        Button removeBtn = createRemoveButton(client);
        actionsBox.add(editBtn, removeBtn);
        return actionsBox;
    }

    private Button createEditButton(Client client) {
        boolean editPermitted = editPermitted(client);

        Button button = uiComponents.create(Button.class);
        button.setTooltipText(messages.getMessage(editPermitted ? "actions.Edit" : "actions.Read"));
        button.setIcon(editPermitted ? VaadinIcon.PENCIL.create() : VaadinIcon.EYE.create());
        button.addClickListener(event -> editClient(client));

        return button;
    }

    private boolean editPermitted(Client client) {
        return getUiEntityContext().isEditPermitted()
                && (getCrudEntityContext().updatePredicate() == null
                || crudEntityContext.isUpdatePermitted(client));
    }

    private void editClient(Client client) {
        viewNavigators.detailView(this, Client.class)
                .editEntity(client)
                .withBackwardNavigation(true)
                .navigate();
    }

    private Button createRemoveButton(Client client) {
        Button button = uiComponents.create(Button.class);
        button.setTooltipText(messages.getMessage("actions.Remove"));
        button.setIcon(VaadinIcon.TRASH.create());
        button.addThemeVariants(ButtonVariant.LUMO_ERROR);
        button.addClickListener(event -> removeClient(client));

        button.setVisible(deletePermitted(client));

        return button;
    }

    private void removeClient(Client client) {
        removeOperation.builder(Client.class, this)
                .withConfirmation(true)
                .withItems(List.of(client))
                .withContainer(clientsDc)
                .remove();
    }

    private boolean deletePermitted(Client client) {
        return getUiEntityContext().isDeletePermitted()
                && (getCrudEntityContext().updatePredicate() == null
                || getCrudEntityContext().isDeletePermitted(client));
    }

    private UiEntityContext getUiEntityContext() {
        if (uiEntityContext == null) {
            MetaClass metaClass = clientsDc.getEntityMetaClass();
            uiEntityContext = new UiEntityContext(metaClass);
            accessManager.applyRegisteredConstraints(uiEntityContext);
        }
        return uiEntityContext;
    }

    private InMemoryCrudEntityContext getCrudEntityContext() {
        if (crudEntityContext == null) {
            crudEntityContext = new InMemoryCrudEntityContext(clientsDc.getEntityMetaClass(), getApplicationContext());
            accessManager.applyRegisteredConstraints(crudEntityContext);
        }

        return crudEntityContext;
    }

//    @Subscribe
//    public void onInit(final InitEvent event) {
//        H2 header = uiComponents.create(H2.class);
//        header.setText("Clients list INIT");
//        getContent().add(header);
//    }
//
//    @Subscribe
//    public void onBeforeShow(final BeforeShowEvent event) {
//        H2 header = uiComponents.create(H2.class);
//        header.setText("Clients list Before Show!");
//        getContent().add(header);
//    }
//
//    @Subscribe
//    public void onReady(final ReadyEvent event) {
//        H2 header = uiComponents.create(H2.class);
//        header.setText("Clients list READY!");
//        getContent().add(header);
//    }


}