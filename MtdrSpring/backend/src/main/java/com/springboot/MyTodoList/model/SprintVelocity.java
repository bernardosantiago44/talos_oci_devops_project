package com.springboot.MyTodoList.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Velocity metrics for a sprint.")
public record SprintVelocity(
        @Schema(description = "Sprint identifier.", example = "sprint-1")
        String sprintId,
        @Schema(description = "Sprint name.", example = "Sprint 1")
        String sprintName,
        @Schema(description = "Sprint status.", example = "ACTIVE")
        String sprintStatus,
        @Schema(description = "Start date.", example = "2026-04-01")
        String startDate,
        @Schema(description = "End date.", example = "2026-04-15")
        String endDate,
        @Schema(description = "Total task count.", example = "10")
        long totalTasks,
        @Schema(description = "Completed task count.", example = "8")
        long completedTasks,
        @Schema(description = "Completion percentage for the sprint.", example = "80.0")
        double fulfillmentPct
) {
}
