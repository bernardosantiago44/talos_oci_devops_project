package com.springboot.MyTodoList.dto.sprint;

import java.time.LocalDate;

public record SprintCreateRequest(
        String sprintId,
        String teamId,
        String name,
        String goal,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        String createdByUserId
) {}
