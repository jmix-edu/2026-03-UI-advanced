package com.company.timesheets.view.mytimeentrylist;


import com.company.timesheets.app.TimeEntrySupport;
import com.company.timesheets.entity.TimeEntry;
import com.company.timesheets.view.main.MainView;
import com.company.timesheets.view.timeentry.TimeEntryDetailView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.facet.Timer;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "my-time-entry-list-view", layout = MainView.class)
@ViewController(id = "ts_TimeEntry.my")
@ViewDescriptor(path = "my-time-entry-list-view.xml")
public class MyTimeEntryListView extends StandardView {

    @ViewComponent
    private DataGrid<TimeEntry> timeEntriesDataGrid;
    @Autowired
    private TimeEntrySupport timeEntrySupport;
    @Autowired
    private DialogWindows dialogWindows;
    @Autowired
    private Notifications notifications;
    @ViewComponent
    private Timer timer;

    @Subscribe("timeEntriesDataGrid.copy")
    public void onTimeEntriesDataGridCopy(final ActionPerformedEvent event) {
        TimeEntry selected = timeEntriesDataGrid.getSingleSelectedItem();
        if (selected == null) {
            return;
        }

        TimeEntry copied = timeEntrySupport.copy(selected);

        dialogWindows.detail(timeEntriesDataGrid)
                .withViewClass(TimeEntryDetailView.class)
                .newEntity(copied)
                .withViewConfigurer(timeEntryDetailView ->
                        timeEntryDetailView.setOwnTimeEntry(true))
                .open();
    }

    @Install(to = "timeEntriesDataGrid.create", subject = "queryParametersProvider")
    private QueryParameters timeEntriesDataGridCreateQueryParametersProvider() {
        return QueryParameters.of(TimeEntryDetailView.PARAMETER_OWN_TIME_ENTRY, "");
    }

    @Install(to = "timeEntriesDataGrid.edit", subject = "queryParametersProvider")
    private QueryParameters timeEntriesDataGridEditQueryParametersProvider() {
        return QueryParameters.of(TimeEntryDetailView.PARAMETER_OWN_TIME_ENTRY, "");
    }

    int seconds = 0;

    @Subscribe(id = "startBtn", subject = "clickListener")
    public void onStartBtnClick(final ClickEvent<JmixButton> event) {
        timer.start();
        notifications.show("Timer started!");
    }

    @Subscribe(id = "stopBtn", subject = "clickListener")
    public void onStopBtnClick(final ClickEvent<JmixButton> event) {
        timer.stop();
    }

    @Subscribe("timer")
    public void onTimerTimerAction(final Timer.TimerActionEvent event) {
        seconds += event.getSource().getDelay() / 1000;
        notifications.show("Timer tick", seconds + " seconds passed");
    }

    @Subscribe("timer")
    public void onTimerTimerStop(final Timer.TimerStopEvent event) {
        notifications.show("Timer stopped");
    }
}