package com.company.timesheets.view.projectrole;

import com.company.timesheets.entity.ProjectRole;
import com.company.timesheets.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "project-roles", layout = MainView.class)
@ViewController("ts_ProjectRole.list")
@ViewDescriptor("project-role-list-view.xml")
@LookupComponent("projectRolesDataGrid")
@DialogMode(width = "64em")
public class ProjectRoleListView extends StandardListView<ProjectRole> {
}