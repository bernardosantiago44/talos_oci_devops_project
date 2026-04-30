package com.springboot.MyTodoList.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dashboard KPI values.")
public record AnalyticsKpis(
        @Schema(description = "Total completed tasks.", example = "12")
        long totalTasks,
        @Schema(description = "Total logged hours.", example = "34.5")
        double totalHours,
        @Schema(description = "Average completed tasks per developer.", example = "4.0")
        double avgTasksPerDev,
        @Schema(description = "Average logged hours per developer.", example = "11.5")
        double avgHoursPerDev
) {
}
