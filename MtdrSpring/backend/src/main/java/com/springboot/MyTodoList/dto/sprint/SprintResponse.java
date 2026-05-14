package com.springboot.MyTodoList.dto.sprint;


import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Schema(description = "Sprint details returned by the API.")
public record SprintResponse(
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
        String status, // TODO: Might be worth it to create a value enum type
        @Schema(description = "Creation timestamp.", example = "2026-04-01T09:00:00-06:00")
        OffsetDateTime createdAt,
        @Schema(description = "Creator user identifier.", example = "user-1")
        String createdByUserId
) {}
