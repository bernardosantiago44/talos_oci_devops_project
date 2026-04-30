package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.AnalyticsDashboard;
import com.springboot.MyTodoList.model.AnalyticsDebug;
import com.springboot.MyTodoList.model.VelocityAnalytics;
import com.springboot.MyTodoList.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Dashboard and sprint velocity analytics.")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get developer dashboard analytics", description = "Returns KPI totals and developer-by-sprint chart data.")
    public AnalyticsDashboard getDashboardData() {
        return analyticsService.getDashboardData();
    }

    @GetMapping("/velocity")
    @Operation(summary = "Get sprint velocity analytics", description = "Returns completion percentages per sprint and the overall velocity target.")
    public VelocityAnalytics getVelocity() {
        return analyticsService.getVelocity();
    }

    @GetMapping("/debug")
    @Operation(summary = "Get analytics debug rows", description = "Returns raw work item, assignment, and time-entry rows used for diagnostics.")
    public AnalyticsDebug debug() {
        return analyticsService.getDebugData();
    }
}
