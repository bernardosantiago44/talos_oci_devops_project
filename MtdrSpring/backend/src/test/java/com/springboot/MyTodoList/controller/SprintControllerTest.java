package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.dto.sprint.SprintResponse;
import com.springboot.MyTodoList.exception.SprintNotFoundException;
import com.springboot.MyTodoList.service.SprintService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SprintController.class)
@AutoConfigureMockMvc(addFilters = false)
class SprintControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SprintService sprintService;

    @Test
    void getAllReturnsSprints() throws Exception {
        when(sprintService.findAll()).thenReturn(List.of(sprintResponse()));

        mockMvc.perform(get("/api/sprints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sprintId").value("sprint-1"))
                .andExpect(jsonPath("$[0].teamId").value("team-1"))
                .andExpect(jsonPath("$[0].name").value("Sprint 1"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void getSprintReturnsSprint() throws Exception {
        when(sprintService.findById("sprint-1")).thenReturn(sprintResponse());

        mockMvc.perform(get("/api/sprints/{id}", "sprint-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sprintId").value("sprint-1"))
                .andExpect(jsonPath("$.goal").value("Deliver analytics dashboard"))
                .andExpect(jsonPath("$.createdByUserId").value("creator-1"));
    }

    @Test
    void getSprintReturnsNotFound() throws Exception {
        when(sprintService.findById("missing"))
                .thenThrow(new SprintNotFoundException("missing"));

        mockMvc.perform(get("/api/sprints/{id}", "missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Sprint not found: missing"));
    }

    private SprintResponse sprintResponse() {
        return new SprintResponse(
                "sprint-1",
                "team-1",
                "Sprint 1",
                "Deliver analytics dashboard",
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 15),
                "ACTIVE",
                OffsetDateTime.parse("2026-04-01T09:00:00-06:00"),
                "creator-1"
        );
    }
}
