package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.AnalyticsDashboard;
import com.springboot.MyTodoList.model.AnalyticsDebug;
import com.springboot.MyTodoList.model.AnalyticsKpis;
import com.springboot.MyTodoList.model.DeveloperSprintAnalytics;
import com.springboot.MyTodoList.model.SprintVelocity;
import com.springboot.MyTodoList.model.VelocityAnalytics;
import com.springboot.MyTodoList.service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalyticsController.class)
@AutoConfigureMockMvc(addFilters = false)
class AnalyticsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsService analyticsService;

    @Test
    void getDashboardDataReturnsDashboardFromService() throws Exception {
        AnalyticsDashboard dashboard = new AnalyticsDashboard(
                new AnalyticsKpis(3, 4.5, 1.5, 2.3),
                List.of(new DeveloperSprintAnalytics("user-1", "Ada Lovelace", "Sprint 1", 4.5, 3))
        );
        when(analyticsService.getDashboardData()).thenReturn(dashboard);

        mockMvc.perform(get("/api/analytics/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kpis.totalTasks").value(3))
                .andExpect(jsonPath("$.kpis.totalHours").value(4.5))
                .andExpect(jsonPath("$.chartData[0].userId").value("user-1"))
                .andExpect(jsonPath("$.chartData[0].developer").value("Ada Lovelace"))
                .andExpect(jsonPath("$.chartData[0].totalHoursWorked").value(4.5))
                .andExpect(jsonPath("$.chartData[0].DEVELOPER").value("Ada Lovelace"))
                .andExpect(jsonPath("$.chartData[0].REAL_HOURS").value(4.5))
                .andExpect(jsonPath("$.chartData[0].TASKS_COMPLETED").value(3));
    }

    @Test
    void getVelocityReturnsVelocityFromService() throws Exception {
        VelocityAnalytics velocity = new VelocityAnalytics(
                84.0,
                75.0,
                List.of(new SprintVelocity("sprint-1", "Sprint 1", "ACTIVE", "2026-04-01", "2026-04-15", 4, 3, 75.0))
        );
        when(analyticsService.getVelocity()).thenReturn(velocity);

        mockMvc.perform(get("/api/analytics/velocity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.target").value(84.0))
                .andExpect(jsonPath("$.overallPct").value(75.0))
                .andExpect(jsonPath("$.sprints[0].sprintId").value("sprint-1"))
                .andExpect(jsonPath("$.sprints[0].fulfillmentPct").value(75.0));
    }

    @Test
    void debugReturnsDebugDataFromService() throws Exception {
        AnalyticsDebug debug = new AnalyticsDebug(
                List.of(Map.of("WORK_ITEM_ID", "wi-d1")),
                List.of(Map.of("ASSIGNMENT_ID", "assignment-1")),
                List.of(Map.of("TIME_ENTRY_ID", "time-1"))
        );
        when(analyticsService.getDebugData()).thenReturn(debug);

        mockMvc.perform(get("/api/analytics/debug"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workItems[0].WORK_ITEM_ID").value("wi-d1"))
                .andExpect(jsonPath("$.assignments[0].ASSIGNMENT_ID").value("assignment-1"))
                .andExpect(jsonPath("$.timeEntries[0].TIME_ENTRY_ID").value("time-1"));
    }
}
