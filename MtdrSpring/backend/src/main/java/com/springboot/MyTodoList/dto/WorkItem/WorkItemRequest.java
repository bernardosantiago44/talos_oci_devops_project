package com.springboot.MyTodoList.dto.WorkItem;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record WorkItemRequest(
        String sprintId,
        String createdByUserId,
        String workType,
        String title,
        String description,
        String status,
        String priority,
        String externalLink,
        Integer estimatedMinutes,
        LocalDate dueDate,
        OffsetDateTime completedAt
) {}
