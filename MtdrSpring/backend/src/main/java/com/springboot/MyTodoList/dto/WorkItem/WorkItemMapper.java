package com.springboot.MyTodoList.dto.WorkItem;

import com.springboot.MyTodoList.model.AppUser;
import com.springboot.MyTodoList.model.WorkItem;

import java.util.Set;
import java.util.stream.Collectors;

public final class WorkItemMapper {
    private WorkItemMapper() {}
    
    public static WorkItemResponse toResponse(WorkItem entity) {
        return new WorkItemResponse(
                entity.getWorkItemId(),
                entity.getSprintId(),
                entity.getCreatedByUserId(),
                entity.getWorkType(),
                entity.getAssignedUsers(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getPriority(),
                entity.getExternalLink(),
                entity.getEstimatedMinutes(),
                entity.getDueDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCompletedAt()
        );
    }
}
