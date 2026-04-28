package com.springboot.MyTodoList.dto.sprint;

import com.springboot.MyTodoList.model.Sprint;

import java.time.OffsetDateTime;

public final class SprintMapper {
    private SprintMapper() {}
    
    public static SprintResponse toResponse(Sprint entity) {
        return new SprintResponse(
                entity.getSprintId(),
                entity.getTeamId(),
                entity.getName(),
                entity.getGoal(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getCreatedByUserId()
        );
    }
    
    public static Sprint toEntity(SprintCreateRequest request) {
        var sprint = new Sprint();
        sprint.setSprintId(request.sprintId());
        sprint.setTeamId(request.teamId());
        sprint.setName(request.name());
        sprint.setGoal(request.goal());
        sprint.setStartDate(request.startDate());
        sprint.setEndDate(request.endDate());
        sprint.setStatus(request.status());
        sprint.setCreatedAt(OffsetDateTime.now());
        sprint.setCreatedByUserId(request.createdByUserId());
        return sprint;
    }
}
