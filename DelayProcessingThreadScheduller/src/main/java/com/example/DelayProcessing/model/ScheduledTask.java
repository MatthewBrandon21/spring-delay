package com.example.DelayProcessing.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ScheduledTask {
    public String taskName;
    public long executeAt;

    @JsonCreator
    public ScheduledTask(@JsonProperty("taskName") String taskName,
                         @JsonProperty("executeAt") long executeAt) {
        this.taskName = taskName;
        this.executeAt = executeAt;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public long getExecuteAt() {
        return executeAt;
    }

    public void setExecuteAt(long executeAt) {
        this.executeAt = executeAt;
    }
}
