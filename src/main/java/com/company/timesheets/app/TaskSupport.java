package com.company.timesheets.app;

import com.company.timesheets.entity.Task;
import com.company.timesheets.entity.User;
import io.jmix.core.DataManager;
import io.jmix.core.querycondition.JpqlCondition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Component("ts_TaskSupport")
public class TaskSupport {

    private final DataManager dataManager;

    public TaskSupport(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public List<Task> getUserActiveTasks(User user) {
        return getActiveTasksInternal(0, 0, user, null);
    }

    public List<Task> getUserActiveTasks(User user, int firstResult, int maxResult) {
        return getActiveTasksInternal(firstResult, maxResult, user, null);
    }

    public Stream<Task> getUserActiveTasks(User user, int firstResult, int maxResult, @Nullable String filter) {
        return getActiveTasksInternal(firstResult, maxResult, user, filter).stream();
    }

    public List<Task> getActiveTasks(int firstResult, int maxResult) {
        return getActiveTasksInternal(firstResult, maxResult, null, null);
    }

    public Stream<Task> getActiveTasks(int firstResult, int maxResult, @Nullable String filter) {
        return getActiveTasksInternal(firstResult, maxResult, null, filter).stream();
    }

    private List<Task> getActiveTasksInternal(int firstResult, int maxResult, @Nullable User user, @Nullable String filter) {
        LogicalCondition condition = createCondition(user, filter);

        return dataManager.load(Task.class)
                .query("select distinct e from ts_Task e " +
                        "where e.project.status = @enum(com.company.timesheets.entity.ProjectStatus.OPEN) " +
                        "and e.status = @enum(com.company.timesheets.entity.TaskStatus.ACTIVE) " +
                        "order by e.project.name asc, e.name asc")
                .firstResult(firstResult)
                .condition(condition)
                .maxResults(maxResult)
                .list();
    }

    private LogicalCondition createCondition(@Nullable User user, @Nullable String filter) {
        PropertyCondition nameCondition = PropertyCondition.contains("name", filter);  // parameter value can be 'null'
        nameCondition.setSkipNullOrEmpty(true);

        JpqlCondition participantCondition = JpqlCondition.createWithParameters(
                "(select pp from ts_ProjectParticipant pp where pp.user = :user) MEMBER OF e.project.participants",
                null, Collections.singletonMap("user", user)
        );
        participantCondition.setSkipNullOrEmpty(true);

        return LogicalCondition.and(
                nameCondition,
                participantCondition
        );
    }
}