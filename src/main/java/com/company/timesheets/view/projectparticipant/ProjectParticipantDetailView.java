package com.company.timesheets.view.projectparticipant;

import com.company.timesheets.entity.Project;
import com.company.timesheets.entity.ProjectParticipant;
import com.company.timesheets.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.flowui.view.*;

@Route(value = "project-participants/:id", layout = MainView.class)
@ViewController("ts_ProjectParticipant.detail")
@ViewDescriptor("project-participant-detail-view.xml")
@EditedEntityContainer("projectParticipantDc")
public class ProjectParticipantDetailView extends StandardDetailView<ProjectParticipant> {

    @ViewComponent
    private EntityPicker<Project> projectField;

    @Subscribe
    public void onReady(final ReadyEvent event) {
        // in case we open a view for an entity creation by direct navigation
        projectField.setVisible(getEditedEntity().getProject() == null);
    }
}