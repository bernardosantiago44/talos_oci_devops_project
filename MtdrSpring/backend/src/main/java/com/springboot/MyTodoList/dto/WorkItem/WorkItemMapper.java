package com.springboot.MyTodoList.dto.WorkItem;

import com.springboot.MyTodoList.model.WorkItem;

public final class WorkItemMapper {
    private WorkItemMapper() {}
    
    public static WorkItemResponse toResponse(WorkItem entity) {
        return new WorkItemResponse(
                entity.getWorkItemId(),
                entity.getSprint(),
                entity.getCreatedByUserId(),
                entity.getWorkType(),
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
