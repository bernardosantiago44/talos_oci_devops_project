package com.springboot.MyTodoList.dto.WorkItem;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Work item details returned by the API.")
public record WorkItemResponse(
        @Schema(description = "Work item identifier.", example = "wi-1")
        String workItemId,
        @Schema(description = "Sprint identifier.", example = "sprint-1")
        String sprintId,
        @Schema(description = "Creator user identifier.", example = "user-1")
        String createdByUserId,
        @Schema(description = "Type of work.", example = "TASK")
        String workType,
        @Schema(description = "Current assignees.")
        List<WorkItemAssignmentDto> assignees,
        @Schema(description = "Short title.", example = "Build analytics dashboard")
        String title,
        @Schema(description = "Detailed description.", example = "Create charts and KPI cards for team analytics.")
        String description,
        @Schema(description = "Current status.", example = "NEW")
        String status,
        @Schema(description = "Priority.", example = "HIGH")
        String priority,
        @Schema(description = "External reference URL.", example = "https://example.com/work/wi-1")
        String externalLink,
        @Schema(description = "Estimated effort in minutes.", example = "120")
        Integer estimatedMinutes,
        @Schema(description = "Due date.", example = "2026-05-15")
        LocalDate dueDate,
        @Schema(description = "Creation timestamp.", example = "2026-04-29T10:00:00-06:00")
        OffsetDateTime createdAt,
        @Schema(description = "Last update timestamp.", example = "2026-04-29T11:00:00-06:00")
        OffsetDateTime updatedAt,
        @Schema(description = "Completion timestamp.", example = "2026-05-15T14:30:00-06:00")
        OffsetDateTime completedAt
) {}
