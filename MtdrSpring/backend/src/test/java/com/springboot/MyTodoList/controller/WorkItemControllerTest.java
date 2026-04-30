package com.springboot.MyTodoList.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.MyTodoList.dto.WorkItem.CreateWorkItemRequest;
import com.springboot.MyTodoList.exception.BusinessRuleException;
import com.springboot.MyTodoList.exception.WorkItemNotFoundException;
import com.springboot.MyTodoList.service.WorkItemAssignmentService;
import com.springboot.MyTodoList.service.WorkItemService;
import com.springboot.MyTodoList.testdata.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkItemController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WorkItemService workItemService;

    @MockitoBean
    private WorkItemAssignmentService assignmentService;

    @Test
    void getAllWorkItemsReturnsWorkItems() throws Exception {
        when(workItemService.findAll()).thenReturn(List.of(TestDataFactory.workItemResponse()));

        mockMvc.perform(get("/api/workitems"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].workItemId").value(TestDataFactory.WORK_ITEM_ID))
                .andExpect(jsonPath("$[0].title").value("Build assignment tests"))
                .andExpect(jsonPath("$[0].assignees[0].user.userId").value(TestDataFactory.ASSIGNEE_USER_ID));
    }

    @Test
    void getWorkItemByIdReturnsWorkItem() throws Exception {
        when(workItemService.findById(TestDataFactory.WORK_ITEM_ID))
                .thenReturn(TestDataFactory.workItemResponse());

        mockMvc.perform(get("/api/workitems/{id}", TestDataFactory.WORK_ITEM_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workItemId").value(TestDataFactory.WORK_ITEM_ID))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void getWorkItemByIdReturnsNotFound() throws Exception {
        when(workItemService.findById("missing"))
                .thenThrow(new WorkItemNotFoundException("missing"));

        mockMvc.perform(get("/api/workitems/{id}", "missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Work Item not found: missing"));
    }

    @Test
    void createWorkItemReturnsCreatedWorkItem() throws Exception {
        CreateWorkItemRequest request = TestDataFactory.validCreateWorkItemRequest();

        when(workItemService.createWorkItem(org.mockito.ArgumentMatchers.any(CreateWorkItemRequest.class)))
                .thenReturn(TestDataFactory.workItemResponse());

        mockMvc.perform(post("/api/workitems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.workItemId").value(TestDataFactory.WORK_ITEM_ID))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    void createWorkItemReturnsValidationErrorForMissingTitle() throws Exception {
        CreateWorkItemRequest request = TestDataFactory.validCreateWorkItemRequest();
        request.setTitle(null);

        mockMvc.perform(post("/api/workitems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fields.title").exists());
    }

    @Test
    void createWorkItemReturnsBusinessRuleError() throws Exception {
        CreateWorkItemRequest request = TestDataFactory.validCreateWorkItemRequest();

        when(workItemService.createWorkItem(org.mockito.ArgumentMatchers.any(CreateWorkItemRequest.class)))
                .thenThrow(new BusinessRuleException("Creator user does not exist: creator-1"));

        mockMvc.perform(post("/api/workitems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BUSINESS_RULE_VIOLATION"))
                .andExpect(jsonPath("$.message").value("Creator user does not exist: creator-1"));
    }

    @Test
    void updateWorkItemReturnsUpdatedWorkItem() throws Exception {
        when(workItemService.updateWorkItem(
                org.mockito.ArgumentMatchers.eq(TestDataFactory.WORK_ITEM_ID),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(TestDataFactory.workItemResponse());

        mockMvc.perform(patch("/api/workitems/{id}", TestDataFactory.WORK_ITEM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestDataFactory.validUpdateWorkItemRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workItemId").value(TestDataFactory.WORK_ITEM_ID));
    }

    @Test
    void getAssigneesReturnsAssignments() throws Exception {
        when(assignmentService.getAssignees(TestDataFactory.WORK_ITEM_ID))
                .thenReturn(List.of(TestDataFactory.assignmentDto()));

        mockMvc.perform(get("/api/workitems/{id}/assignees", TestDataFactory.WORK_ITEM_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].assignmentId").value(TestDataFactory.ASSIGNMENT_ID))
                .andExpect(jsonPath("$[0].user.userId").value(TestDataFactory.ASSIGNEE_USER_ID));
    }

    @Test
    void addAssigneeReturnsCreatedAssignment() throws Exception {
        when(assignmentService.addAssignee(TestDataFactory.WORK_ITEM_ID, TestDataFactory.ASSIGNEE_USER_ID))
                .thenReturn(TestDataFactory.assignmentDto());

        mockMvc.perform(patch(
                        "/api/workitems/{id}/assignees/{userId}",
                        TestDataFactory.WORK_ITEM_ID,
                        TestDataFactory.ASSIGNEE_USER_ID
                ))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assignmentRole").value("ASSIGNEE"));
    }

    @Test
    void removeAssigneeReturnsNoContent() throws Exception {
        doNothing().when(assignmentService)
                .removeAssignee(TestDataFactory.WORK_ITEM_ID, TestDataFactory.ASSIGNEE_USER_ID);

        mockMvc.perform(delete(
                        "/api/workitems/{id}/assignees/{userId}",
                        TestDataFactory.WORK_ITEM_ID,
                        TestDataFactory.ASSIGNEE_USER_ID
                ))
                .andExpect(status().isNoContent());

        verify(assignmentService).removeAssignee(TestDataFactory.WORK_ITEM_ID, TestDataFactory.ASSIGNEE_USER_ID);
    }

    @Test
    void deleteWorkItemReturnsNoContent() throws Exception {
        doNothing().when(workItemService).deleteWorkItemById(TestDataFactory.WORK_ITEM_ID);

        mockMvc.perform(delete("/api/workitems/{id}", TestDataFactory.WORK_ITEM_ID))
                .andExpect(status().isNoContent());

        verify(workItemService).deleteWorkItemById(TestDataFactory.WORK_ITEM_ID);
    }
}
