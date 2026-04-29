package com.springboot.MyTodoList.dto.WorkItem;

import java.time.OffsetDateTime;

public record WorkItemAssignmentDto(
        String assignmentId,
        String assignmentRole,
        OffsetDateTime assignedAt,
        OffsetDateTime unassignedAt,
        AssignedUserDto user,
        AssignedUserDto assignedBy
) {}
