package com.springboot.MyTodoList.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Dashboard analytics response.")
public record AnalyticsDashboard(
        @Schema(description = "Dashboard KPI totals and averages.")
        AnalyticsKpis kpis,
        @Schema(description = "Developer and sprint chart rows.")
        List<DeveloperSprintAnalytics> chartData
) {
}
