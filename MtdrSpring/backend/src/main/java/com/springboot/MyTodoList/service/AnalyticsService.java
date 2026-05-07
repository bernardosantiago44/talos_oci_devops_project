package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.AnalyticsDashboard;
import com.springboot.MyTodoList.model.AnalyticsDebug;
import com.springboot.MyTodoList.model.AnalyticsKpis;
import com.springboot.MyTodoList.model.DeveloperSprintAnalytics;
import com.springboot.MyTodoList.model.SprintVelocity;
import com.springboot.MyTodoList.model.SprintVelocityRow;
import com.springboot.MyTodoList.model.VelocityAnalytics;
import com.springboot.MyTodoList.repository.AnalyticsRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class AnalyticsService {
    private static final double VELOCITY_TARGET = 84.0;

    private final AnalyticsRepository analyticsRepository;

    public AnalyticsService(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    public AnalyticsDashboard getDashboardData() {
        List<DeveloperSprintAnalytics> chartData = analyticsRepository.findDeveloperSprintAnalytics();

        long totalTasks = chartData.stream()
                .mapToLong(DeveloperSprintAnalytics::tasksCompleted)
                .sum();
        double totalHours = chartData.stream()
                .mapToDouble(DeveloperSprintAnalytics::totalHoursWorked)
                .sum();

        Set<String> developerIds = new LinkedHashSet<>();
        chartData.forEach(row -> developerIds.add(row.userId()));
        int numDevs = developerIds.isEmpty() ? 1 : developerIds.size();

        AnalyticsKpis kpis = new AnalyticsKpis(
                totalTasks,
                round1(totalHours),
                round1((double) totalTasks / numDevs),
                round1(totalHours / numDevs)
        );

        return new AnalyticsDashboard(kpis, chartData);
    }

    public VelocityAnalytics getVelocity() {
        List<SprintVelocity> sprints = analyticsRepository.findSprintVelocityRows()
                .stream()
                .map(this::toSprintVelocity)
                .toList();

        long totalAll = sprints.stream()
                .mapToLong(SprintVelocity::totalTasks)
                .sum();
        long completedAll = sprints.stream()
                .mapToLong(SprintVelocity::completedTasks)
                .sum();
        double overallPct = totalAll == 0 ? 0.0 : round1((double) completedAll * 100.0 / totalAll);

        return new VelocityAnalytics(VELOCITY_TARGET, overallPct, sprints);
    }

    public AnalyticsDebug getDebugData() {
        return analyticsRepository.findDebugData();
    }

    private SprintVelocity toSprintVelocity(SprintVelocityRow row) {
        double fulfillmentPct = row.totalTasks() == 0
                ? 0.0
                : round1((double) row.completedTasks() * 100.0 / row.totalTasks());

        return new SprintVelocity(
                row.sprintId(),
                row.sprintName(),
                row.sprintStatus(),
                row.startDate(),
                row.endDate(),
                row.totalTasks(),
                row.completedTasks(),
                fulfillmentPct
        );
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
