package com.springboot.MyTodoList.dto.sprint;


import java.time.LocalDate;
import java.time.OffsetDateTime;

public record SprintResponse(
        String sprintId,
        String teamId,
        String name,
        String goal, 
        LocalDate startDate,
        LocalDate endDate,
        String status, // TODO: Might be worth it to create a value enum type
        OffsetDateTime createdAt,
        String createdByUserId
) {}