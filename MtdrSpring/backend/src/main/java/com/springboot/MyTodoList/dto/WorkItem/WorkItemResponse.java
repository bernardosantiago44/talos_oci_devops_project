package com.springboot.MyTodoList.dto.WorkItem;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record WorkItemResponse(
        String workItemId,
        String sprintId,
        String createdByUserId,
        String workType,
        List<WorkItemAssignmentDto> assignments,
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
