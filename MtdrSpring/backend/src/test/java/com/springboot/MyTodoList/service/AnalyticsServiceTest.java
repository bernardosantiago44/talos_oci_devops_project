package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.AnalyticsDashboard;
import com.springboot.MyTodoList.model.DeveloperSprintAnalytics;
import com.springboot.MyTodoList.model.SprintVelocityRow;
import com.springboot.MyTodoList.model.VelocityAnalytics;
import com.springboot.MyTodoList.repository.AnalyticsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {
    @Mock
    private AnalyticsRepository analyticsRepository;

    @InjectMocks
    private AnalyticsService service;

    @Test
    void getDashboardDataCalculatesKpisPerDistinctDeveloper() {
        when(analyticsRepository.findDeveloperSprintAnalytics()).thenReturn(List.of(
                new DeveloperSprintAnalytics("user-1", "Ada Lovelace", "Sprint 1", 1.25, 2),
                new DeveloperSprintAnalytics("user-1", "Ada Lovelace", "Sprint 2", 0.75, 1),
                new DeveloperSprintAnalytics("user-2", "Grace Hopper", "Sprint 1", 2.0, 3)
        ));

        AnalyticsDashboard dashboard = service.getDashboardData();

        assertThat(dashboard.kpis().totalTasks()).isEqualTo(6);
        assertThat(dashboard.kpis().totalHours()).isEqualTo(4.0);
        assertThat(dashboard.kpis().avgTasksPerDev()).isEqualTo(3.0);
        assertThat(dashboard.kpis().avgHoursPerDev()).isEqualTo(2.0);
        assertThat(dashboard.chartData()).hasSize(3);
    }

    @Test
    void getDashboardDataHandlesNoRows() {
        when(analyticsRepository.findDeveloperSprintAnalytics()).thenReturn(List.of());

        AnalyticsDashboard dashboard = service.getDashboardData();

        assertThat(dashboard.kpis().totalTasks()).isZero();
        assertThat(dashboard.kpis().totalHours()).isZero();
        assertThat(dashboard.kpis().avgTasksPerDev()).isZero();
        assertThat(dashboard.kpis().avgHoursPerDev()).isZero();
        assertThat(dashboard.chartData()).isEmpty();
    }

    @Test
    void getVelocityCalculatesSprintAndOverallFulfillment() {
        when(analyticsRepository.findSprintVelocityRows()).thenReturn(List.of(
                new SprintVelocityRow("sprint-1", "Sprint 1", "CLOSED", "2026-04-01", "2026-04-15", 4, 3),
                new SprintVelocityRow("sprint-2", "Sprint 2", "ACTIVE", "2026-04-16", "2026-04-30", 2, 1)
        ));

        VelocityAnalytics velocity = service.getVelocity();

        assertThat(velocity.target()).isEqualTo(84.0);
        assertThat(velocity.overallPct()).isEqualTo(66.7);
        assertThat(velocity.sprints())
                .extracting("fulfillmentPct")
                .containsExactly(75.0, 50.0);
    }

    @Test
    void getVelocityHandlesZeroTaskSprint() {
        when(analyticsRepository.findSprintVelocityRows()).thenReturn(List.of(
                new SprintVelocityRow("sprint-1", "Sprint 1", "PLANNED", "2026-04-01", null, 0, 0)
        ));

        VelocityAnalytics velocity = service.getVelocity();

        assertThat(velocity.overallPct()).isZero();
        assertThat(velocity.sprints().getFirst().fulfillmentPct()).isZero();
    }
}
