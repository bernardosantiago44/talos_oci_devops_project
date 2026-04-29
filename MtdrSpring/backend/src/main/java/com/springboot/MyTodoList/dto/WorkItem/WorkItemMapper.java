package com.springboot.MyTodoList.dto.WorkItem;

import com.springboot.MyTodoList.model.AppUser;
import com.springboot.MyTodoList.model.WorkItem;
import com.springboot.MyTodoList.model.WorkItemAssignment;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
}
