package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.WorkItem.CreateWorkItemRequest;
import com.springboot.MyTodoList.dto.WorkItem.UpdateWorkItemRequest;
import com.springboot.MyTodoList.model.WorkItem;
import com.springboot.MyTodoList.repository.AppUserRepository;
import com.springboot.MyTodoList.repository.SprintRepository;
import com.springboot.MyTodoList.repository.WorkItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkItemServiceTest {
    @Mock
    private WorkItemRepository workItemRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private WorkItemAssignmentService assignmentService;

    @InjectMocks
    private WorkItemService service;

    @Test
    void createWorkItemSavesWorkItemOnly() {
        CreateWorkItemRequest request = validCreateRequest();

        when(appUserRepository.existsById("creator-1")).thenReturn(true);
        when(sprintRepository.existsById("sprint-1")).thenReturn(true);
        when(workItemRepository.save(any(WorkItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createWorkItem(request);

        verify(workItemRepository).save(any(WorkItem.class));
        verify(assignmentService, never()).replaceAssignees(any(), any());
    }

    @Test
    void createWorkItemReplacesAssigneesWhenAssigneeIdsArePresent() {
        CreateWorkItemRequest request = validCreateRequest();
        request.setAssigneeIds(java.util.List.of("user-1", "user-2"));

        when(appUserRepository.existsById("creator-1")).thenReturn(true);
        when(sprintRepository.existsById("sprint-1")).thenReturn(true);
        when(workItemRepository.save(any(WorkItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createWorkItem(request);

        verify(assignmentService).replaceAssignees(any(WorkItem.class), org.mockito.ArgumentMatchers.eq(request.getAssigneeIds()));
    }

    @Test
    void updateWorkItemUpdatesWorkItemOnly() {
        WorkItem existing = new WorkItem();
        existing.setWorkItemId("wi-1");

        UpdateWorkItemRequest request = new UpdateWorkItemRequest();
        request.setTitle("Updated title");

        when(workItemRepository.findById("wi-1")).thenReturn(Optional.of(existing));
        when(workItemRepository.save(existing)).thenReturn(existing);

        service.updateWorkItem("wi-1", request);

        verify(workItemRepository).save(existing);
        verify(assignmentService, never()).replaceAssignees(any(), any());
    }

    @Test
    void updateWorkItemReplacesAssigneesWhenAssigneeIdsArePresent() {
        WorkItem existing = new WorkItem();
        existing.setWorkItemId("wi-1");

        UpdateWorkItemRequest request = new UpdateWorkItemRequest();
        request.setAssigneeIds(java.util.List.of("user-2", "user-3"));

        when(workItemRepository.findById("wi-1")).thenReturn(Optional.of(existing));
        when(workItemRepository.save(existing)).thenReturn(existing);

        service.updateWorkItem("wi-1", request);

        verify(assignmentService).replaceAssignees(existing, request.getAssigneeIds());
    }

    @Test
    void updateWorkItemReplacesAssigneesWithEmptyList() {
        WorkItem existing = new WorkItem();
        existing.setWorkItemId("wi-1");

        UpdateWorkItemRequest request = new UpdateWorkItemRequest();
        request.setAssigneeIds(java.util.List.of());

        when(workItemRepository.findById("wi-1")).thenReturn(Optional.of(existing));
        when(workItemRepository.save(existing)).thenReturn(existing);

        service.updateWorkItem("wi-1", request);

        verify(assignmentService).replaceAssignees(existing, request.getAssigneeIds());
    }

    private static CreateWorkItemRequest validCreateRequest() {
        CreateWorkItemRequest request = new CreateWorkItemRequest();
        request.setSprintId("sprint-1");
        request.setCreatedByUserId("creator-1");
        request.setWorkType("TASK");
        request.setTitle("Build assignments");
        request.setStatus("NEW");
        request.setPriority("HIGH");
        request.setEstimatedMinutes(30);
        return request;
    }

}
