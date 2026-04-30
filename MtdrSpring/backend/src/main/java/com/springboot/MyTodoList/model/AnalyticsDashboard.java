package com.springboot.MyTodoList.model;

import java.util.List;

public record AnalyticsDashboard(
        AnalyticsKpis kpis,
        List<DeveloperSprintAnalytics> chartData
) {
}
