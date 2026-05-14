package com.springboot.MyTodoList.dto.sprint;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Request payload for creating a sprint.")
public record SprintCreateRequest(
        @Schema(description = "Sprint identifier.", example = "sprint-1")
        String sprintId,
        @Schema(description = "Team identifier.", example = "team-1")
        String teamId,
        @Schema(description = "Sprint name.", example = "Sprint 1")
        String name,
        @Schema(description = "Sprint goal.", example = "Deliver analytics dashboard")
        String goal,
        @Schema(description = "Start date.", example = "2026-04-01")
        LocalDate startDate,
        @Schema(description = "End date.", example = "2026-04-15")
        LocalDate endDate,
        @Schema(description = "Sprint status.", example = "ACTIVE")
        String status,
        @Schema(description = "Creator user identifier.", example = "user-1")
        String createdByUserId
) {}
