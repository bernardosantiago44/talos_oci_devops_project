package com.springboot.MyTodoList.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Worked hours and completed task count for one developer in one sprint.")
public record DeveloperSprintAnalytics(
        @Schema(description = "User identifier.", example = "user-1")
        String userId,
        @Schema(description = "Developer display name.", example = "Ada Lovelace")
        String developer,
        @Schema(description = "Sprint name.", example = "Sprint 1")
        String sprint,
        @Schema(description = "Total logged hours for the developer and sprint.", example = "8.5")
        double totalHoursWorked,
        @Schema(description = "Completed task count for the developer and sprint.", example = "3")
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
