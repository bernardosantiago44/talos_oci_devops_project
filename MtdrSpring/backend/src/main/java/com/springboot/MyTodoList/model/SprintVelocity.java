package com.springboot.MyTodoList.model;

public record SprintVelocity(
        String sprintId,
        String sprintName,
        String sprintStatus,
        String startDate,
        String endDate,
        long totalTasks,
        long completedTasks,
        double fulfillmentPct
) {
}
