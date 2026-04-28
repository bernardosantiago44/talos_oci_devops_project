package com.springboot.MyTodoList.dto.WorkItem;

import com.springboot.MyTodoList.model.Sprint;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record WorkItemResponse(
        String workItemId,
        Sprint sprint,
        String createdByUserId,
        String workType,
        String title,
        String description,
        String status,
        String priority,
        String externalLink,
        Integer estimatedMinutes,
        LocalDate dueDate,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime completedAt
) {}
