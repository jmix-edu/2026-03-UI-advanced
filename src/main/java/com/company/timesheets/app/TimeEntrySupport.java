package com.company.timesheets.app;

import com.company.timesheets.entity.TimeEntry;
import com.company.timesheets.entity.TimeEntryStatus;
import io.jmix.core.*;
import org.springframework.stereotype.Component;

@Component("ts_TimeEntrySupport")
public class TimeEntrySupport {

    private final MetadataTools metadataTools;
    private final DataManager dataManager;

    public TimeEntrySupport(MetadataTools metadataTools, DataManager dataManager) {
        this.metadataTools = metadataTools;
        this.dataManager = dataManager;
    }

    public TimeEntry copy(TimeEntry source) {
        TimeEntry reloaded = dataManager.load(Id.of(source))
                .fetchPlan(planBuilder ->
                        planBuilder.addFetchPlan(FetchPlan.BASE)
                                .add("user", FetchPlan.INSTANCE_NAME)
                                .add("task", FetchPlan.INSTANCE_NAME))
                .one();

        TimeEntry copied = metadataTools.copy(reloaded);
        copied.setId(UuidProvider.createUuid());
        copied.setStatus(TimeEntryStatus.NEW);
        return copied;
    }
}