package com.springboot.MyTodoList.dto.WorkItem;

import com.springboot.MyTodoList.model.AppUser;
import com.springboot.MyTodoList.model.WorkItem;
import com.springboot.MyTodoList.model.WorkItemAssignment;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class WorkItemMapper {
    private WorkItemMapper() {}
    
    public static WorkItemResponse toResponse(WorkItem entity) {
        List<WorkItemAssignmentDto> assignments = entity
                .getAssignments()
                .stream()
                .map(WorkItemMapper::toAssignmentDto)
                .toList();
        
        return new WorkItemResponse(
                entity.getWorkItemId(),
                entity.getSprintId(),
                entity.getCreatedByUserId(),
                entity.getWorkType(),
                assignments,
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

    private static WorkItemAssignmentDto toAssignmentDto(WorkItemAssignment assignment) {
        return new WorkItemAssignmentDto(
                assignment.getAssignmentId(),
                assignment.getAssignmentRole(),
                assignment.getAssignedAt(),
                assignment.getUnassignedAt(),
                toUserDto(assignment.getAssignedUser()),
                toUserDto(assignment.getAssignedByUser())
        );
    }

    private static AssignedUserDto toUserDto(AppUser user) {
        if (user == null) {
            return null;
        }

        return new AssignedUserDto(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getTelegramUserId()
        );
    }
    
    public static WorkItem fromCreateRequest(CreateWorkItemRequest request) {
        WorkItem workItem = new WorkItem();

        workItem.setWorkItemId(UUID.randomUUID().toString());
        workItem.setSprintId(request.getSprintId());
        workItem.setCreatedByUserId(request.getCreatedByUserId());
        workItem.setWorkType(request.getWorkType());
        workItem.setTitle(request.getTitle());
        workItem.setDescription(workItem.getDescription());
        workItem.setStatus("NEW");
        workItem.setPriority(request.getPriority());
        workItem.setEstimatedMinutes(request.getEstimatedMinutes());
        workItem.setDueDate(request.getDueDate());
        workItem.setCreatedAt(OffsetDateTime.now());
        workItem.setUpdatedAt(OffsetDateTime.now());
        
        return workItem;
    }
    
    public static void applyUpdates(WorkItem workItem, UpdateWorkItemRequest request) {
        if (request.getSprintId() != null) {
            workItem.setSprintId(request.getSprintId());
        }

        if (request.getWorkType() != null) {
            workItem.setWorkType(request.getWorkType());
        }

        if (request.getTitle() != null) {
            workItem.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            workItem.setDescription(request.getDescription());
        }

        if (request.getStatus() != null) {
            workItem.setStatus(request.getStatus());
        }

        if (request.getPriority() != null) {
            workItem.setPriority(request.getPriority());
        }

        if (request.getExternalLink() != null) {
            workItem.setExternalLink(request.getExternalLink());
        }

        if (request.getEstimatedMinutes() != null) {
            workItem.setEstimatedMinutes(request.getEstimatedMinutes());
        }

        if (request.getDueDate() != null) {
            workItem.setDueDate(request.getDueDate());
        }

        if (request.getCompletedAt() != null) {
            workItem.setCompletedAt(request.getCompletedAt());
        }

        workItem.setUpdatedAt(OffsetDateTime.now());
        
        if (request.getStatus() != null && request.getStatus().equals("DONE")) {
            workItem.setCompletedAt(OffsetDateTime.now());
        }
    }
}
