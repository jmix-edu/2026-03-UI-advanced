package com.company.timesheets.view.task;

import com.company.timesheets.entity.Project;
import com.company.timesheets.entity.Task;
import com.company.timesheets.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.flowui.view.*;

@Route(value = "tasks/:id", layout = MainView.class)
@ViewController("ts_Task.detail")
@ViewDescriptor("task-detail-view.xml")
@EditedEntityContainer("taskDc")
public class TaskDetailView extends StandardDetailView<Task> {

    @ViewComponent
    private EntityPicker<Project> projectField;

    @Subscribe
    public void onReady(final ReadyEvent event) {
        // in case we open a view for an entity creation by direct navigation
        projectField.setVisible(getEditedEntity().getProject() == null);
    }
}