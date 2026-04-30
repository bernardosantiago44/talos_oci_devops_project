package com.springboot.MyTodoList.model;

import java.util.List;

public record VelocityAnalytics(
        double target,
        double overallPct,
        List<SprintVelocity> sprints
) {
}
