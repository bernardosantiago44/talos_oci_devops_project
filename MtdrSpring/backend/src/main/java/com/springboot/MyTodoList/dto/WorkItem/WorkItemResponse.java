package com.springboot.MyTodoList.dto.WorkItem;

import com.springboot.MyTodoList.model.AppUser;
import com.springboot.MyTodoList.model.Assignee;
import com.springboot.MyTodoList.model.Sprint;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;

public record WorkItemResponse(
        String workItemId,
        String sprintId,
        String createdByUserId,
        String workType,
        Set<Assignee> assignedUsers,
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
