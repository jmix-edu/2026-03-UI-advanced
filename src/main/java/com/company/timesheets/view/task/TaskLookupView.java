package com.company.timesheets.view.task;

import com.company.timesheets.entity.Task;
import io.jmix.flowui.view.*;

@ViewController("ts_Task.lookup")
@ViewDescriptor("task-lookup-view.xml")
@LookupComponent("tasksDataGrid")
@DialogMode(width = "64em")
public class TaskLookupView extends StandardListView<Task> {
}