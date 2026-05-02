package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.WorkItem.CreateWorkItemRequest;
import com.springboot.MyTodoList.dto.WorkItem.UpdateWorkItemRequest;
import com.springboot.MyTodoList.dto.WorkItem.WorkItemResponse;
import com.springboot.MyTodoList.exception.AppUserNotFoundException;
import com.springboot.MyTodoList.exception.BusinessRuleException;
import com.springboot.MyTodoList.exception.WorkItemNotFoundException;
import com.springboot.MyTodoList.model.WorkItem;
import com.springboot.MyTodoList.model.WorkItemPriority;
import com.springboot.MyTodoList.repository.AppUserRepository;
import com.springboot.MyTodoList.repository.SprintRepository;
import com.springboot.MyTodoList.repository.WorkItemRepository;
import com.springboot.MyTodoList.testdata.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
    void findByValidId() {
        when(workItemRepository.findById(any())).thenReturn(Optional.of(TestDataFactory.workItem("wi-001")));
        
        assertThat(service.findById("wi-001").workItemId()).isEqualTo("wi-001");
    }
    
    @Test
    void createWorkItemSavesWorkItemOnly() {
        CreateWorkItemRequest request = TestDataFactory.validCreateWorkItemRequest();
        request.setEstimatedMinutes(null);

        when(appUserRepository.existsById(TestDataFactory.CREATOR_USER_ID)).thenReturn(true);
        when(sprintRepository.existsById(TestDataFactory.SPRINT_ID)).thenReturn(true);
        when(workItemRepository.save(any(WorkItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createWorkItem(request);

        ArgumentCaptor<WorkItem> captor = ArgumentCaptor.forClass(WorkItem.class);
        verify(workItemRepository).save(captor.capture());
        assertThat(captor.getValue().getPriority()).isEqualTo(WorkItemPriority.HIGH);
        assertThat(captor.getValue().getWorkItemId()).isNotBlank();
        assertThat(captor.getValue().getSprintId()).isEqualTo(TestDataFactory.SPRINT_ID);
        assertThat(captor.getValue().getCreatedByUserId()).isEqualTo(TestDataFactory.CREATOR_USER_ID);
        assertThat(captor.getValue().getWorkType()).isEqualTo("TASK");
        assertThat(captor.getValue().getTitle()).isEqualTo("Build assignment tests");
        assertThat(captor.getValue().getStatus()).isEqualTo("NEW");
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
        assertThat(captor.getValue().getUpdatedAt()).isNotNull();
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

        assertThat(existing.getPriority()).isEqualTo(WorkItemPriority.MEDIUM);
        assertThat(existing.getTitle()).isEqualTo("Updated title");
        assertThat(existing.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(existing.getEstimatedMinutes()).isEqualTo(45);
        assertThat(existing.getUpdatedAt()).isNotEqualTo(OffsetDateTime.parse("2026-04-29T10:00:00-06:00"));
        verify(workItemRepository).save(existing);
        verify(assignmentService, never()).replaceAssignees(any(), any());
    }

    @Test
    void updateWorkItemAppliesAllOptionalFieldsAndMarksDoneCompleted() {
        WorkItem existing = TestDataFactory.workItem();
        OffsetDateTime explicitCompletedAt = OffsetDateTime.parse("2026-05-01T10:00:00-06:00");

        UpdateWorkItemRequest request = new UpdateWorkItemRequest();
        request.setSprintId("sprint-2");
        request.setWorkType("BUG");
        request.setTitle("Fix login validation");
        request.setDescription("Reject blank credentials");
        request.setStatus("DONE");
        request.setPriority(WorkItemPriority.LOW);
        request.setExternalLink("https://example.com/work/wi-2");
        request.setEstimatedMinutes(15);
        request.setDueDate(java.time.LocalDate.of(2026, 5, 20));
        request.setCompletedAt(explicitCompletedAt);

        when(workItemRepository.findById(TestDataFactory.WORK_ITEM_ID)).thenReturn(Optional.of(existing));
        when(workItemRepository.save(existing)).thenReturn(existing);

        service.updateWorkItem(TestDataFactory.WORK_ITEM_ID, request);

        assertThat(existing.getSprintId()).isEqualTo("sprint-2");
        assertThat(existing.getWorkType()).isEqualTo("BUG");
        assertThat(existing.getTitle()).isEqualTo("Fix login validation");
        assertThat(existing.getDescription()).isEqualTo("Reject blank credentials");
        assertThat(existing.getStatus()).isEqualTo("DONE");
        assertThat(existing.getPriority()).isEqualTo(WorkItemPriority.LOW);
        assertThat(existing.getExternalLink()).isEqualTo("https://example.com/work/wi-2");
        assertThat(existing.getEstimatedMinutes()).isEqualTo(15);
        assertThat(existing.getDueDate()).isEqualTo(java.time.LocalDate.of(2026, 5, 20));
        assertThat(existing.getCompletedAt()).isNotNull();
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
    void updateWorkItemRejectsBlankWorkType() {
        WorkItem existing = TestDataFactory.workItem();
        UpdateWorkItemRequest request = new UpdateWorkItemRequest();
        request.setWorkType(" ");

        when(workItemRepository.findById(TestDataFactory.WORK_ITEM_ID)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.updateWorkItem(TestDataFactory.WORK_ITEM_ID, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Work type cannot be blank");

        verify(workItemRepository, never()).save(any());
    }

    @Test
    void updateWorkItemRejectsBlankStatus() {
        WorkItem existing = TestDataFactory.workItem();
        UpdateWorkItemRequest request = new UpdateWorkItemRequest();
        request.setStatus(" ");

        when(workItemRepository.findById(TestDataFactory.WORK_ITEM_ID)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.updateWorkItem(TestDataFactory.WORK_ITEM_ID, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Status cannot be blank");

        verify(workItemRepository, never()).save(any());
    }

    @Test
    void updateWorkItemRejectsNegativeEstimate() {
        WorkItem existing = TestDataFactory.workItem();
        UpdateWorkItemRequest request = new UpdateWorkItemRequest();
        request.setEstimatedMinutes(-1);

        when(workItemRepository.findById(TestDataFactory.WORK_ITEM_ID)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.updateWorkItem(TestDataFactory.WORK_ITEM_ID, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Estimated minutes cannot be negative");

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

    @Test
    void findAllReturnsListOfWorkItemResponse() {
        WorkItem existing = TestDataFactory.workItem();

        when(workItemRepository.findAll()).thenReturn(List.of(existing));

        assertThat(service.findAll().getFirst()).isInstanceOf(WorkItemResponse.class);
        verify(workItemRepository, times(1)).findAll();
    }

    @Test
    void findByIdReturnsNotFoundException() {
        when(workItemRepository.findById(any())).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> service.findById("wi-1"))
                .isInstanceOf(WorkItemNotFoundException.class)
                .hasMessageContaining("wi-1");
        verify(workItemRepository, times(1)).findById("wi-1");
    }
    
    @Test
    void findByInvalidTelegramUserIdReturnsNotFoundException() {
        when(appUserRepository.findByTelegramUserId(any())).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> service.findByTelegramUserId("u-001"))
                .isInstanceOf(AppUserNotFoundException.class)
                .hasMessageContaining("telegram::u-001");
        verify(workItemRepository, never()).findByTelegramUserId(any());
    }
    
    @Test
    void findByValidTelegramUserIdReturnsWorkItems() {
        when(appUserRepository.findByTelegramUserId("telegram-001"))
                .thenReturn(Optional.of(TestDataFactory.appUser("001")));
        when(workItemRepository.findByTelegramUserId("telegram-001"))
                .thenReturn(List.of(TestDataFactory.workItem("wi-001")));
        
        assertThat(service.findByTelegramUserId("telegram-001").getFirst())
                .isInstanceOf(WorkItemResponse.class);
    }

    @Test
    void deleteWorkItemByIdDeletesExistingWorkItem() {
        when(workItemRepository.existsById(TestDataFactory.WORK_ITEM_ID)).thenReturn(true);

        service.deleteWorkItemById(TestDataFactory.WORK_ITEM_ID);

        verify(workItemRepository).deleteById(TestDataFactory.WORK_ITEM_ID);
    }

    @Test
    void deleteWorkItemByIdThrowsWhenWorkItemDoesNotExist() {
        when(workItemRepository.existsById("missing")).thenReturn(false);

        assertThatThrownBy(() -> service.deleteWorkItemById("missing"))
                .isInstanceOf(WorkItemNotFoundException.class)
                .hasMessage("Work Item not found: missing");

        verify(workItemRepository, never()).deleteById(any());
    }

}
