package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.timeEntry.TimeEntryRequest;
import com.springboot.MyTodoList.dto.timeEntry.TimeEntryResponse;
import com.springboot.MyTodoList.exception.AppUserNotFoundException;
import com.springboot.MyTodoList.exception.BusinessRuleException;
import com.springboot.MyTodoList.exception.WorkItemNotFoundException;
import com.springboot.MyTodoList.model.TimeEntry;
import com.springboot.MyTodoList.repository.AppUserRepository;
import com.springboot.MyTodoList.repository.TimeEntryRepository;
import com.springboot.MyTodoList.repository.WorkItemRepository;
import com.springboot.MyTodoList.testdata.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeEntryServiceTest {
    @Mock
    private TimeEntryRepository timeEntryRepository;

    @Mock
    private WorkItemRepository workItemRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private TimeEntryService service;

    @Test
    void logTimeSavesTimeEntryAndReturnsResponse() {
        TimeEntryRequest request = validRequest();

        when(workItemRepository.existsById(TestDataFactory.WORK_ITEM_ID)).thenReturn(true);
        when(appUserRepository.existsById(TestDataFactory.ASSIGNEE_USER_ID)).thenReturn(true);
        when(workItemRepository.findById(TestDataFactory.WORK_ITEM_ID))
                .thenReturn(Optional.of(TestDataFactory.workItem()));
        when(appUserRepository.findById(TestDataFactory.ASSIGNEE_USER_ID))
                .thenReturn(Optional.of(TestDataFactory.appUser(TestDataFactory.ASSIGNEE_USER_ID)));
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TimeEntryResponse response = service.logTime(request);

        ArgumentCaptor<TimeEntry> captor = ArgumentCaptor.forClass(TimeEntry.class);
        verify(timeEntryRepository).save(captor.capture());
        TimeEntry saved = captor.getValue();

        assertThat(saved.getId()).isNotBlank();
        assertThat(saved.getWorkItem().getWorkItemId()).isEqualTo(TestDataFactory.WORK_ITEM_ID);
        assertThat(saved.getUser().getUserId()).isEqualTo(TestDataFactory.ASSIGNEE_USER_ID);
        assertThat(saved.getMinutes()).isEqualTo(90);
        assertThat(saved.getNote()).isEqualTo("Implemented service tests");
        assertThat(saved.getCreatedAt()).isNotNull();

        assertThat(response.getWorkItemId()).isEqualTo(TestDataFactory.WORK_ITEM_ID);
        assertThat(response.getUserId()).isEqualTo(TestDataFactory.ASSIGNEE_USER_ID);
        assertThat(response.getMinutes()).isEqualTo(90);
        assertThat(response.getNote()).isEqualTo("Implemented service tests");
    }

    @Test
    void logTimeUsesEmptyNoteWhenRequestNoteIsNull() {
        TimeEntryRequest request = validRequest();
        request.setNote(null);

        when(workItemRepository.existsById(TestDataFactory.WORK_ITEM_ID)).thenReturn(true);
        when(appUserRepository.existsById(TestDataFactory.ASSIGNEE_USER_ID)).thenReturn(true);
        when(workItemRepository.findById(TestDataFactory.WORK_ITEM_ID))
                .thenReturn(Optional.of(TestDataFactory.workItem()));
        when(appUserRepository.findById(TestDataFactory.ASSIGNEE_USER_ID))
                .thenReturn(Optional.of(TestDataFactory.appUser(TestDataFactory.ASSIGNEE_USER_ID)));
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TimeEntryResponse response = service.logTime(request);

        assertThat(response.getNote()).isEmpty();
    }

    @Test
    void logTimeRejectsNonPositiveMinutes() {
        TimeEntryRequest request = validRequest();
        request.setMinutes(0);

        assertThatThrownBy(() -> service.logTime(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Minutes must be a positive integer: 0");

        verify(timeEntryRepository, never()).save(any());
    }

    @Test
    void logTimeRejectsMissingWorkItemDuringValidation() {
        TimeEntryRequest request = validRequest();

        when(workItemRepository.existsById(TestDataFactory.WORK_ITEM_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.logTime(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Work Item does not exist: " + TestDataFactory.WORK_ITEM_ID);

        verify(appUserRepository, never()).existsById(any());
        verify(timeEntryRepository, never()).save(any());
    }

    @Test
    void logTimeRejectsMissingUserDuringValidation() {
        TimeEntryRequest request = validRequest();

        when(workItemRepository.existsById(TestDataFactory.WORK_ITEM_ID)).thenReturn(true);
        when(appUserRepository.existsById(TestDataFactory.ASSIGNEE_USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.logTime(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("User id does not exist" + TestDataFactory.ASSIGNEE_USER_ID);

        verify(timeEntryRepository, never()).save(any());
    }

    @Test
    void logTimeThrowsWhenWorkItemDisappearsBeforeSave() {
        TimeEntryRequest request = validRequest();

        when(workItemRepository.existsById(TestDataFactory.WORK_ITEM_ID)).thenReturn(true);
        when(appUserRepository.existsById(TestDataFactory.ASSIGNEE_USER_ID)).thenReturn(true);
        when(workItemRepository.findById(TestDataFactory.WORK_ITEM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.logTime(request))
                .isInstanceOf(WorkItemNotFoundException.class)
                .hasMessage("Work Item not found: " + TestDataFactory.WORK_ITEM_ID);

        verify(timeEntryRepository, never()).save(any());
    }

    @Test
    void logTimeThrowsWhenUserDisappearsBeforeSave() {
        TimeEntryRequest request = validRequest();

        when(workItemRepository.existsById(TestDataFactory.WORK_ITEM_ID)).thenReturn(true);
        when(appUserRepository.existsById(TestDataFactory.ASSIGNEE_USER_ID)).thenReturn(true);
        when(workItemRepository.findById(TestDataFactory.WORK_ITEM_ID))
                .thenReturn(Optional.of(TestDataFactory.workItem()));
        when(appUserRepository.findById(TestDataFactory.ASSIGNEE_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.logTime(request))
                .isInstanceOf(AppUserNotFoundException.class)
                .hasMessage("App user with id " + TestDataFactory.ASSIGNEE_USER_ID + " does not exist.");

        verify(timeEntryRepository, never()).save(any());
    }

    private TimeEntryRequest validRequest() {
        TimeEntryRequest request = new TimeEntryRequest();
        request.setWorkItemId(TestDataFactory.WORK_ITEM_ID);
        request.setUserId(TestDataFactory.ASSIGNEE_USER_ID);
        request.setMinutes(90);
        request.setNote("Implemented service tests");
        return request;
    }
}
