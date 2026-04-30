package com.springboot.MyTodoList.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Sprint velocity analytics response.")
public record VelocityAnalytics(
        @Schema(description = "Target fulfillment percentage.", example = "84.0")
        double target,
        @Schema(description = "Overall fulfillment percentage across all sprints.", example = "76.3")
        double overallPct,
        @Schema(description = "Per-sprint velocity rows.")
        List<SprintVelocity> sprints
) {
}
