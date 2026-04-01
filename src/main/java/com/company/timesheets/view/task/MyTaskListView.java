package com.company.timesheets.view.task;


import com.company.timesheets.entity.Task;
import com.company.timesheets.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "my-tasks", layout = MainView.class)
@ViewController("ts_Task.my")
@ViewDescriptor("my-task-list-view.xml")
@LookupComponent("tasksDataGrid")
@DialogMode(width = "64em")
public class MyTaskListView extends StandardListView<Task> {
}