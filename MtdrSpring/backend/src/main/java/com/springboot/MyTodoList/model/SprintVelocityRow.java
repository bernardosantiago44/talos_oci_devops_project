package com.springboot.MyTodoList.model;

public record SprintVelocityRow(
        String sprintId,
        String sprintName,
        String sprintStatus,
        String startDate,
        String endDate,
        long totalTasks,
        long completedTasks
) {
}
