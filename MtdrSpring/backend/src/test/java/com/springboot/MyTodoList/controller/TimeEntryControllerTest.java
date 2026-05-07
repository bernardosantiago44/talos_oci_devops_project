package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.dto.timeEntry.TimeEntryRequest;
import com.springboot.MyTodoList.dto.timeEntry.TimeEntryResponse;
import com.springboot.MyTodoList.exception.BusinessRuleException;
import com.springboot.MyTodoList.service.TimeEntryService;
import com.springboot.MyTodoList.testdata.TestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TimeEntryController.class)
@AutoConfigureMockMvc(addFilters = false)
class TimeEntryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TimeEntryService timeEntryService;

    @Test
    void logTimeReturnsTimeEntry() throws Exception {
        TimeEntryRequest request = validTimeEntryRequest();

        when(timeEntryService.logTime(any(TimeEntryRequest.class)))
                .thenReturn(new TimeEntryResponse(
                        "Implemented controller tests",
                        90,
                        TestDataFactory.ASSIGNEE_USER_ID,
                        TestDataFactory.WORK_ITEM_ID
                ));

        mockMvc.perform(post("/api/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workItemId").value(TestDataFactory.WORK_ITEM_ID))
                .andExpect(jsonPath("$.userId").value(TestDataFactory.ASSIGNEE_USER_ID))
                .andExpect(jsonPath("$.minutes").value(90))
                .andExpect(jsonPath("$.note").value("Implemented controller tests"));
    }

    @Test
    void logTimeReturnsValidationErrorForMissingWorkItemId() throws Exception {
        TimeEntryRequest request = validTimeEntryRequest();
        request.setWorkItemId(null);

        mockMvc.perform(post("/api/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fields.workItemId").exists());

        verify(timeEntryService, never()).logTime(any());
    }

    @Test
    void logTimeReturnsValidationErrorForInvalidMinutes() throws Exception {
        TimeEntryRequest request = validTimeEntryRequest();
        request.setMinutes(0);

        mockMvc.perform(post("/api/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fields.minutes").exists());

        verify(timeEntryService, never()).logTime(any());
    }

    @Test
    void logTimeReturnsBusinessRuleError() throws Exception {
        TimeEntryRequest request = validTimeEntryRequest();

        when(timeEntryService.logTime(any(TimeEntryRequest.class)))
                .thenThrow(new BusinessRuleException("Work Item does not exist: wi-missing"));

        mockMvc.perform(post("/api/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BUSINESS_RULE_VIOLATION"))
                .andExpect(jsonPath("$.message").value("Work Item does not exist: wi-missing"));
    }

    private TimeEntryRequest validTimeEntryRequest() {
        TimeEntryRequest request = new TimeEntryRequest();
        request.setWorkItemId(TestDataFactory.WORK_ITEM_ID);
        request.setUserId(TestDataFactory.ASSIGNEE_USER_ID);
        request.setMinutes(90);
        request.setNote("Implemented controller tests");
        return request;
    }
}
