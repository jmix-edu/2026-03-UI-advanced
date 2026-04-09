package com.company.timesheets.view.project;

import com.company.timesheets.entity.Project;
import com.company.timesheets.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "projects", layout = MainView.class)
@ViewController("ts_Project.list")
@ViewDescriptor("project-list-view.xml")
@LookupComponent("projectsDataGrid")
@DialogMode(width = "64em")
public class ProjectListView extends StandardListView<Project> {
    @Install(to = "projectsDataGrid.status", subject = "partNameGenerator")
    private String projectsDataGridStatusPartNameGenerator(final Project project) {
        return switch (project.getStatus()) {
            case OPEN -> "open-project";
            case CLOSED -> "closed-project";
        };
    }
}