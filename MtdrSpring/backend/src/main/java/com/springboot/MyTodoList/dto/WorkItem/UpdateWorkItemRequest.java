package com.springboot.MyTodoList.dto.WorkItem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Schema(description = "Request payload for updating a work item. Only non-null fields are applied.")
public final class UpdateWorkItemRequest {
    @Schema(description = "Sprint that contains the work item.", example = "sprint-1")
    private String sprintId;

    @Schema(description = "Type of work.", example = "BUG")
    private String workType;

    @Schema(description = "Short title for the work item.", example = "Fix login validation")
    private String title;

    @Schema(description = "Detailed work item description.", example = "Validation should reject blank credentials.")
    private String description;

    @Schema(description = "Current work item status.", example = "IN_PROGRESS")
    private String status;

    @Schema(description = "Work item priority.", example = "MEDIUM")
    private String priority;

    @Schema(description = "External reference URL.", example = "https://example.com/work/wi-1")
    private String externalLink;

    @Schema(description = "Estimated effort in minutes.", example = "45")
    private Integer estimatedMinutes;

    @Schema(description = "Due date in ISO-8601 format.", example = "2026-05-15")
    private LocalDate dueDate;

    @Schema(description = "Completion timestamp.", example = "2026-05-15T14:30:00-06:00")
    private OffsetDateTime completedAt;

    @Schema(description = "Replacement set of assigned user IDs.", example = "[\"user-1\", \"user-2\"]")
    private List<String> assigneeIds;
}
