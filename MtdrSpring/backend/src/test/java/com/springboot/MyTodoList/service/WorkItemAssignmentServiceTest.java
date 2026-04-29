package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.exception.BusinessRuleException;
import com.springboot.MyTodoList.exception.WorkItemNotFoundException;
import com.springboot.MyTodoList.model.AppUser;
import com.springboot.MyTodoList.model.WorkItem;
import com.springboot.MyTodoList.model.WorkItemAssignment;
import com.springboot.MyTodoList.repository.AppUserRepository;
import com.springboot.MyTodoList.repository.WorkItemAssignmentRepository;
import com.springboot.MyTodoList.repository.WorkItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkItemAssignmentServiceTest {
    @Mock
    private WorkItemRepository workItemRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private WorkItemAssignmentRepository assignmentRepository;

    @InjectMocks
    private WorkItemAssignmentService service;

    @Test
    void addAssigneeCreatesAssignment() {
        WorkItem workItem = workItem("wi-1");
        AppUser user = appUser("user-1");

        when(workItemRepository.findById("wi-1")).thenReturn(Optional.of(workItem));
        when(appUserRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(assignmentRepository.existsByWorkItem_WorkItemIdAndAssignedUser_UserId("wi-1", "user-1"))
                .thenReturn(false);
        when(assignmentRepository.save(any(WorkItemAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.addAssignee("wi-1", "user-1");

        ArgumentCaptor<WorkItemAssignment> captor = ArgumentCaptor.forClass(WorkItemAssignment.class);
        verify(assignmentRepository).save(captor.capture());
        WorkItemAssignment saved = captor.getValue();

        assertThat(saved.getAssignmentId()).isNotBlank();
        assertThat(saved.getWorkItem()).isSameAs(workItem);
        assertThat(saved.getAssignedUser()).isSameAs(user);
        assertThat(saved.getAssignmentRole()).isEqualTo("ASSIGNEE");
        assertThat(saved.getAssignedAt()).isNotNull();
        assertThat(saved.getUnassignedAt()).isNull();
        assertThat(saved.getAssignedByUser()).isNull();
    }

    @Test
    void addAssigneeRejectsDuplicateAssignment() {
        when(workItemRepository.findById("wi-1")).thenReturn(Optional.of(workItem("wi-1")));
        when(appUserRepository.findById("user-1")).thenReturn(Optional.of(appUser("user-1")));
        when(assignmentRepository.existsByWorkItem_WorkItemIdAndAssignedUser_UserId("wi-1", "user-1"))
                .thenReturn(true);

        assertThatThrownBy(() -> service.addAssignee("wi-1", "user-1"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("User is already assigned to this work item");

        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void addAssigneeRejectsMissingWorkItem() {
        when(workItemRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addAssignee("missing", "user-1"))
                .isInstanceOf(WorkItemNotFoundException.class);

        verify(appUserRepository, never()).findById(any());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void addAssigneeRejectsMissingUser() {
        when(workItemRepository.findById("wi-1")).thenReturn(Optional.of(workItem("wi-1")));
        when(appUserRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addAssignee("wi-1", "missing"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("User does not exist: missing");

        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void removeAssigneeDeletesAssignmentWhenItExists() {
        WorkItemAssignment assignment = new WorkItemAssignment();
        assignment.setAssignmentId("assignment-1");

        when(assignmentRepository.findByWorkItem_WorkItemIdAndAssignedUser_UserId("wi-1", "user-1"))
                .thenReturn(Optional.of(assignment));

        service.removeAssignee("wi-1", "user-1");

        verify(assignmentRepository).delete(assignment);
    }

    @Test
    void removeAssigneeDoesNothingWhenAssignmentDoesNotExist() {
        when(assignmentRepository.findByWorkItem_WorkItemIdAndAssignedUser_UserId("wi-1", "user-1"))
                .thenReturn(Optional.empty());

        service.removeAssignee("wi-1", "user-1");

        verify(assignmentRepository, never()).delete(any());
    }

    @Test
    void getAssigneesReturnsAssignmentsForExistingWorkItem() {
        WorkItem workItem = workItem("wi-1");
        AppUser user = appUser("user-1");
        WorkItemAssignment assignment = new WorkItemAssignment();
        assignment.setAssignmentId("assignment-1");
        assignment.setWorkItem(workItem);
        assignment.setAssignedUser(user);
        assignment.setAssignmentRole("ASSIGNEE");

        when(workItemRepository.existsById("wi-1")).thenReturn(true);
        when(assignmentRepository.findByWorkItem_WorkItemId("wi-1")).thenReturn(List.of(assignment));

        var assignees = service.getAssignees("wi-1");

        assertThat(assignees).hasSize(1);
        assertThat(assignees.get(0).assignmentId()).isEqualTo("assignment-1");
        assertThat(assignees.get(0).user().userId()).isEqualTo("user-1");
    }

    @Test
    void replaceAssigneesDeletesExistingAndCreatesRequestedAssignments() {
        WorkItem workItem = workItem("wi-1");
        AppUser firstUser = appUser("user-1");
        AppUser secondUser = appUser("user-2");

        when(appUserRepository.findById("user-1")).thenReturn(Optional.of(firstUser));
        when(appUserRepository.findById("user-2")).thenReturn(Optional.of(secondUser));
        when(assignmentRepository.save(any(WorkItemAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.replaceAssignees(workItem, List.of("user-1", "user-2"));

        ArgumentCaptor<WorkItemAssignment> captor = ArgumentCaptor.forClass(WorkItemAssignment.class);
        verify(assignmentRepository).deleteByWorkItem_WorkItemId("wi-1");
        verify(assignmentRepository, org.mockito.Mockito.times(2)).save(captor.capture());

        assertThat(captor.getAllValues()).hasSize(2);
        assertThat(captor.getAllValues())
                .allSatisfy(assignment -> {
                    assertThat(assignment.getAssignmentId()).isNotBlank();
                    assertThat(assignment.getWorkItem()).isSameAs(workItem);
                    assertThat(assignment.getAssignmentRole()).isEqualTo("ASSIGNEE");
                    assertThat(assignment.getAssignedAt()).isNotNull();
                    assertThat(assignment.getUnassignedAt()).isNull();
                });
        assertThat(workItem.getAssignments()).hasSize(2);
    }

    @Test
    void replaceAssigneesWithEmptyListRemovesAllAssignments() {
        WorkItem workItem = workItem("wi-1");
        workItem.getAssignments().add(new WorkItemAssignment());

        service.replaceAssignees(workItem, List.of());

        verify(assignmentRepository).deleteByWorkItem_WorkItemId("wi-1");
        verify(assignmentRepository, never()).save(any());
        assertThat(workItem.getAssignments()).isEmpty();
    }

    @Test
    void replaceAssigneesRejectsDuplicateUserIdsBeforeDeletingExistingAssignments() {
        WorkItem workItem = workItem("wi-1");

        assertThatThrownBy(() -> service.replaceAssignees(workItem, List.of("user-1", "user-1")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Duplicate assignees are not allowed");

        verify(assignmentRepository, never()).deleteByWorkItem_WorkItemId(any());
        verify(assignmentRepository, never()).save(any());
    }

    private static WorkItem workItem(String id) {
        WorkItem workItem = new WorkItem();
        workItem.setWorkItemId(id);
        return workItem;
    }

    private static AppUser appUser(String id) {
        AppUser user = new AppUser();
        user.setUserId(id);
        return user;
    }
}
