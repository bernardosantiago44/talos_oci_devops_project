package com.springboot.MyTodoList.dto.WorkItem;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Assignment details for a work item assignee.")
public record WorkItemAssignmentDto(
        @Schema(description = "Assignment identifier.", example = "assignment-1")
        String assignmentId,
        @Schema(description = "Role for the assignment.", example = "ASSIGNEE")
        String assignmentRole,
        @Schema(description = "Assignment timestamp.", example = "2026-04-29T11:00:00-06:00")
        OffsetDateTime assignedAt,
        @Schema(description = "Unassignment timestamp when no longer active.", example = "2026-04-30T11:00:00-06:00")
        OffsetDateTime unassignedAt,
        @Schema(description = "Assigned user.")
        AssignedUserDto user,
        @Schema(description = "User who created the assignment.")
        AssignedUserDto assignedBy
) {}
