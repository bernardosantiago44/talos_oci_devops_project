package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.AnalyticsDashboard;
import com.springboot.MyTodoList.model.AnalyticsDebug;
import com.springboot.MyTodoList.model.VelocityAnalytics;
import com.springboot.MyTodoList.service.AnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/analytics/dashboard")
    public AnalyticsDashboard getDashboardData() {
        return analyticsService.getDashboardData();
    }

    @GetMapping("/analytics/velocity")
    public VelocityAnalytics getVelocity() {
        return analyticsService.getVelocity();
    }

    @GetMapping("/analytics/debug")
    public AnalyticsDebug debug() {
        return analyticsService.getDebugData();
    }
}
