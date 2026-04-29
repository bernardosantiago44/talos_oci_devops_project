package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.WorkItem.CreateWorkItemRequest;
import com.springboot.MyTodoList.dto.WorkItem.UpdateWorkItemRequest;
import com.springboot.MyTodoList.exception.BusinessRuleException;
import com.springboot.MyTodoList.exception.WorkItemNotFoundException;
import com.springboot.MyTodoList.model.WorkItem;
import com.springboot.MyTodoList.repository.AppUserRepository;
import com.springboot.MyTodoList.repository.SprintRepository;
import com.springboot.MyTodoList.repository.WorkItemRepository;
import com.springboot.MyTodoList.testdata.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        CreateWorkItemRequest request = TestDataFactory.validCreateWorkItemRequest();

        when(appUserRepository.existsById(TestDataFactory.CREATOR_USER_ID)).thenReturn(true);
        when(sprintRepository.existsById(TestDataFactory.SPRINT_ID)).thenReturn(true);
        when(workItemRepository.save(any(WorkItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createWorkItem(request);

        verify(workItemRepository).save(any(WorkItem.class));
        verify(assignmentService, never()).replaceAssignees(any(), any());
    }

    @Test
    void createWorkItemReplacesAssigneesWhenAssigneeIdsArePresent() {
        CreateWorkItemRequest request = TestDataFactory.validCreateWorkItemRequest();
        request.setAssigneeIds(List.of("user-1", "user-2"));

        when(appUserRepository.existsById(TestDataFactory.CREATOR_USER_ID)).thenReturn(true);
        when(sprintRepository.existsById(TestDataFactory.SPRINT_ID)).thenReturn(true);
        when(workItemRepository.save(any(WorkItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createWorkItem(request);

        verify(assignmentService).replaceAssignees(any(WorkItem.class), eq(request.getAssigneeIds()));
    }

    @Test
    void createWorkItemRejectsUnknownCreator() {
        CreateWorkItemRequest request = TestDataFactory.validCreateWorkItemRequest();

        when(appUserRepository.existsById(TestDataFactory.CREATOR_USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.createWorkItem(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Creator user does not exist: " + TestDataFactory.CREATOR_USER_ID);

        verify(workItemRepository, never()).save(any());
    }

    @Test
    void createWorkItemRejectsUnknownSprint() {
        CreateWorkItemRequest request = TestDataFactory.validCreateWorkItemRequest();

        when(appUserRepository.existsById(TestDataFactory.CREATOR_USER_ID)).thenReturn(true);
        when(sprintRepository.existsById(TestDataFactory.SPRINT_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.createWorkItem(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Sprint does not exist: " + TestDataFactory.SPRINT_ID);

        verify(workItemRepository, never()).save(any());
    }

    @Test
    void createWorkItemRejectsNonPositiveEstimate() {
        CreateWorkItemRequest request = TestDataFactory.validCreateWorkItemRequest();
        request.setEstimatedMinutes(0);

        when(appUserRepository.existsById(TestDataFactory.CREATOR_USER_ID)).thenReturn(true);
        when(sprintRepository.existsById(TestDataFactory.SPRINT_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.createWorkItem(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Estimated minutes must be greater than zero");

        verify(workItemRepository, never()).save(any());
    }

    @Test
    void updateWorkItemUpdatesWorkItemOnly() {
        WorkItem existing = TestDataFactory.workItem();

        UpdateWorkItemRequest request = TestDataFactory.validUpdateWorkItemRequest();

        when(workItemRepository.findById(TestDataFactory.WORK_ITEM_ID)).thenReturn(Optional.of(existing));
        when(workItemRepository.save(existing)).thenReturn(existing);

        service.updateWorkItem(TestDataFactory.WORK_ITEM_ID, request);

        verify(workItemRepository).save(existing);
        verify(assignmentService, never()).replaceAssignees(any(), any());
    }

    @Test
    void updateWorkItemReplacesAssigneesWhenAssigneeIdsArePresent() {
        WorkItem existing = TestDataFactory.workItem();

        UpdateWorkItemRequest request = new UpdateWorkItemRequest();
        request.setAssigneeIds(List.of("user-2", "user-3"));

        when(workItemRepository.findById(TestDataFactory.WORK_ITEM_ID)).thenReturn(Optional.of(existing));
        when(workItemRepository.save(existing)).thenReturn(existing);

        service.updateWorkItem(TestDataFactory.WORK_ITEM_ID, request);

        verify(assignmentService).replaceAssignees(existing, request.getAssigneeIds());
    }

    @Test
    void updateWorkItemReplacesAssigneesWithEmptyList() {
        WorkItem existing = TestDataFactory.workItem();

        UpdateWorkItemRequest request = new UpdateWorkItemRequest();
        request.setAssigneeIds(List.of());

        when(workItemRepository.findById(TestDataFactory.WORK_ITEM_ID)).thenReturn(Optional.of(existing));
        when(workItemRepository.save(existing)).thenReturn(existing);

        service.updateWorkItem(TestDataFactory.WORK_ITEM_ID, request);

        verify(assignmentService).replaceAssignees(existing, request.getAssigneeIds());
    }

    @Test
    void updateWorkItemRejectsMissingWorkItem() {
        UpdateWorkItemRequest request = TestDataFactory.validUpdateWorkItemRequest();

        when(workItemRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateWorkItem("missing", request))
                .isInstanceOf(WorkItemNotFoundException.class)
                .hasMessage("Work Item not found: missing");

        verify(workItemRepository, never()).save(any());
    }

    @Test
    void updateWorkItemRejectsBlankTitle() {
        WorkItem existing = TestDataFactory.workItem();
        UpdateWorkItemRequest request = new UpdateWorkItemRequest();
        request.setTitle(" ");

        when(workItemRepository.findById(TestDataFactory.WORK_ITEM_ID)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.updateWorkItem(TestDataFactory.WORK_ITEM_ID, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Title cannot be blank");

        verify(workItemRepository, never()).save(any());
    }

    @Test
    void updateWorkItemRejectsCompletedWorkItem() {
        WorkItem existing = TestDataFactory.workItem();
        existing.setCompletedAt(OffsetDateTime.parse("2026-04-29T12:00:00-06:00"));
        UpdateWorkItemRequest request = TestDataFactory.validUpdateWorkItemRequest();

        when(workItemRepository.findById(TestDataFactory.WORK_ITEM_ID)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.updateWorkItem(TestDataFactory.WORK_ITEM_ID, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Completed work items cannot be updated");

        verify(workItemRepository, never()).save(any());
    }

}
