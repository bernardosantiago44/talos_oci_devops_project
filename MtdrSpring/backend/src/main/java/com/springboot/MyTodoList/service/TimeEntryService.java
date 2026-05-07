package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.timeEntry.TimeEntryRequest;
import com.springboot.MyTodoList.dto.timeEntry.TimeEntryResponse;
import com.springboot.MyTodoList.exception.AppUserNotFoundException;
import com.springboot.MyTodoList.exception.BusinessRuleException;
import com.springboot.MyTodoList.exception.WorkItemNotFoundException;
import com.springboot.MyTodoList.model.AppUser;
import com.springboot.MyTodoList.model.TimeEntry;
import com.springboot.MyTodoList.model.WorkItem;
import com.springboot.MyTodoList.repository.AppUserRepository;
import com.springboot.MyTodoList.repository.TimeEntryRepository;
import com.springboot.MyTodoList.repository.WorkItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class TimeEntryService {
    private static final Logger log = LoggerFactory.getLogger(TimeEntryService.class);
    private final TimeEntryRepository timeEntryRepository;
    private final WorkItemRepository workItemRepository;
    private final AppUserRepository appUserRepository;

    public TimeEntryService(TimeEntryRepository timeEntryRepository, WorkItemRepository workItemRepository, AppUserRepository appUserRepository) {
        this.timeEntryRepository = timeEntryRepository;
        this.workItemRepository = workItemRepository;
        this.appUserRepository = appUserRepository;
    }

    public TimeEntryResponse logTime(TimeEntryRequest request) {
        validateRequest(request);
        
        TimeEntry timeEntry = timeEntryRepository.save(toTimeEntry(request));
        return toResponse(timeEntry);
    }
    
    private void validateRequest(TimeEntryRequest request) {
        if (request.getMinutes() <= 0) {
            log.warn("Invalid time entry minutes: {}", request.getMinutes());
            throw new BusinessRuleException("Minutes must be a positive integer: " + request.getMinutes());
        }
        
        if (!workItemRepository.existsById(request.getWorkItemId())) {
            log.warn("Work Item does not exist: {}", request.getWorkItemId());
            throw new BusinessRuleException("Work Item does not exist: " + request.getWorkItemId());
        }
        
        if (!appUserRepository.existsById(request.getUserId())) {
            log.warn("User id does not exist: {}", request.getUserId());
            throw new BusinessRuleException("User id does not exist" + request.getUserId());
        }
    }
    
    private TimeEntry toTimeEntry(TimeEntryRequest request) {
        TimeEntry timeEntry = new TimeEntry();
        WorkItem workItem = findWorkItem(request.getWorkItemId());
        AppUser user = findAppUser(request.getUserId());
        
        timeEntry.setId(UUID.randomUUID().toString());
        timeEntry.setWorkItem(workItem);
        timeEntry.setUser(user);
        timeEntry.setNote(request.getNote());
        timeEntry.setMinutes(request.getMinutes());
        timeEntry.setCreatedAt(OffsetDateTime.now());
        
        return timeEntry;
    }
    
    private TimeEntryResponse toResponse(TimeEntry timeEntry) {
        return new TimeEntryResponse(timeEntry.getNote(), 
                timeEntry.getMinutes(), 
                timeEntry.getUser().getUserId(), 
                timeEntry.getWorkItem().getWorkItemId()
        );
    }

    private WorkItem findWorkItem(String workItemId) {
        return workItemRepository
                .findById(workItemId)
                .orElseThrow(() -> new WorkItemNotFoundException(workItemId));
    }
    
    private AppUser findAppUser(String userId) {
        return appUserRepository
                .findById(userId)
                .orElseThrow(() -> new AppUserNotFoundException(userId));
    }
}
