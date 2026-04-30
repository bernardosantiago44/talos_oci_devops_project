package com.springboot.MyTodoList.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DeveloperSprintAnalytics(
        String userId,
        String developer,
        String sprint,
        double totalHoursWorked,
        long tasksCompleted
) {
    @JsonProperty("USER_ID")
    public String legacyUserId() {
        return userId;
    }

    @JsonProperty("DEVELOPER")
    public String legacyDeveloper() {
        return developer;
    }

    @JsonProperty("SPRINT")
    public String legacySprint() {
        return sprint;
    }

    @JsonProperty("SPRINT_NAME")
    public String legacySprintName() {
        return sprint;
    }

    @JsonProperty("TOTAL_HOURS_WORKED")
    public double legacyTotalHoursWorked() {
        return totalHoursWorked;
    }

    @JsonProperty("REAL_HOURS")
    public double legacyRealHours() {
        return totalHoursWorked;
    }

    @JsonProperty("TASKS_COMPLETED")
    public long legacyTasksCompleted() {
        return tasksCompleted;
    }
}
