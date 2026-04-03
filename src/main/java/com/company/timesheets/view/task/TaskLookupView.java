package com.company.timesheets.view.task;

import com.company.timesheets.entity.Task;
import com.company.timesheets.entity.User;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.lang.Nullable;


@ViewController("ts_Task.lookup")
@ViewDescriptor("task-lookup-view.xml")
@LookupComponent("tasksDataGrid")
@DialogMode(width = "64em")
public class TaskLookupView extends StandardListView<Task> {
    @ViewComponent
    private CollectionLoader<Task> tasksDl;

    public void setFilterUser(@Nullable User user) {
        if (user != null) {
            tasksDl.setParameter("user", user);
        } else {
            tasksDl.removeParameter("user");
        }
    }
}