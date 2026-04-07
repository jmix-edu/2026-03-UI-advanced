package com.company.timesheets.view.user;

import com.company.timesheets.entity.User;
import com.company.timesheets.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.app.inputdialog.DialogActions;
import io.jmix.flowui.app.inputdialog.DialogOutcome;
import io.jmix.flowui.app.inputdialog.InputParameter;
import io.jmix.flowui.asynctask.UiAsyncTasks;
import io.jmix.flowui.backgroundtask.BackgroundTask;
import io.jmix.flowui.backgroundtask.BackgroundTaskHandler;
import io.jmix.flowui.backgroundtask.BackgroundWorker;
import io.jmix.flowui.backgroundtask.TaskLifeCycle;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.lang.Thread.sleep;

@Route(value = "users", layout = MainView.class)
@ViewController(id = "ts_User.list")
@ViewDescriptor(path = "user-list-view.xml")
@LookupComponent("usersDataGrid")
@DialogMode(width = "64em")
public class UserListView extends StandardListView<User> {
    @Autowired
    private Dialogs dialogs;
    @Autowired
    private UiComponents uiComponents;
    @ViewComponent
    private DataGrid<User> usersDataGrid;
    @ViewComponent
    private CollectionContainer<User> usersDc;
    @Autowired
    private Notifications notifications;
    @Autowired
    private BackgroundWorker backgroundWorker;
    @Autowired
    private UiAsyncTasks uiAsyncTasks;
    @ViewComponent
    private TypedTextField<Object> inputField;

    @Subscribe("usersDataGrid.sendEmail")
    public void onUsersDataGridSendEmail(final ActionPerformedEvent event) {
        dialogs.createInputDialog(this)
                .withHeader("Send email(s)")
                .withLabelsPosition(Dialogs.InputDialogBuilder.LabelsPosition.TOP)
                .withParameters(
                        InputParameter.stringParameter("title")
                                .withLabel("Title")
                                .withRequired(true),
                        InputParameter.parameter("body")
                                .withLabel("Body")
                                .withField(() -> {
                                    JmixTextArea textArea = uiComponents.create(JmixTextArea.class);
                                    textArea.setRequired(true);
                                    textArea.setWidthFull();
                                    textArea.setHeight("9.5em");

                                    return textArea;
                                })
                )
                .withActions(DialogActions.OK_CANCEL)
                .withCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(DialogOutcome.OK)) {
                        String title = closeEvent.getValue("title");
                        String body = closeEvent.getValue("body");

                        Set<User> selected = usersDataGrid.getSelectedItems();
                        Collection<User> users = selected.isEmpty()
                                ? usersDc.getItems()
                                : selected;

                        doSendEmail(title, body, users);
                    }
                })
                .open();

    }

    private void doSendEmail(String title, String body, Collection<User> users) {
        BackgroundTask<Integer, Void> task = new EmailTask(title, body, users);
        BackgroundTaskHandler<Void> handler = backgroundWorker.handle(task);
        handler.execute();
//        handler.getResult();

//        dialogs.createBackgroundTaskDialog(task)
//                .withHeader("Please, wait")
//                .withText("Email are being sent...")
//                .withTotal(users.size())
//                .withShowProgressInPercentage(true)
//                .withCancelAllowed(true)
//                .open();
    }

    @Subscribe(id = "performWithoutResultBtn", subject = "clickListener")
    public void onPerformWithoutResultBtnClick(final ClickEvent<JmixButton> event) {
        uiAsyncTasks.runnableConfigurer(this::voidMethod)
                .withResultHandler(() -> {
                    notifications.show("Action performed!");
                })
                .runAsync();
    }

    private void voidMethod() {
        try {
            sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted!", e);
        }
    }

    @Subscribe(id = "performChangesBtn", subject = "clickListener")
    public void onPerformChangesBtnClick(final ClickEvent<JmixButton> event) {
        String typed = inputField.getValue();

        uiAsyncTasks.supplierConfigurer(() -> changeString(typed))
                .withResultHandler(changed -> {
                    notifications.show(changed);
                })
                .withTimeout(2, TimeUnit.SECONDS)
                .withExceptionHandler(ex -> {
                    if (ex instanceof TimeoutException) {
                        notifications.create("Timeout exception!")
                                .withType(Notifications.Type.WARNING)
                                .show();
                    } else {
                        notifications.create("Unknown exception!" + ex.getMessage())
                                .withType(Notifications.Type.WARNING)
                                .show();
                    }
                })
                .supplyAsync();
    }

    private String changeString(String typed) {
        try {
            sleep(4000);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted!", e);
        }

        return (typed + " changed").toUpperCase();
    }

    private class EmailTask extends BackgroundTask<Integer, Void> {

        private final String title;
        private final String body;
        private final Collection<User> users;

        private EmailTask(String title, String body, Collection<User> users) {
            super(10, TimeUnit.MINUTES, UserListView.this);

            this.title = title;
            this.body = body;
            this.users = users;
        }

        @Override
        public Void run(TaskLifeCycle<Integer> taskLifeCycle) throws Exception {
            int i = 0;
            for (User user : users) {
                if (taskLifeCycle.isInterrupted() || taskLifeCycle.isCancelled()) {
                    break;
                }

                TimeUnit.SECONDS.sleep(2);

                i++;
                taskLifeCycle.publish(i);
            }

            return null;
        }

        @Override
        public void done(Void result) {
            notifications.create("Emails have been sent")
                    .withType(Notifications.Type.SUCCESS)
                    .withPosition(Notification.Position.TOP_END)
                    .show();
        }
    }


}