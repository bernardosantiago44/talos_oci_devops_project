package com.springboot.MyTodoList.model;

public record AnalyticsKpis(
        long totalTasks,
        double totalHours,
        double avgTasksPerDev,
        double avgHoursPerDev
) {
}
