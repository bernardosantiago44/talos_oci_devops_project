package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.WorkItem.*;
import com.springboot.MyTodoList.exception.AppUserNotFoundException;
import com.springboot.MyTodoList.exception.BusinessRuleException;
import com.springboot.MyTodoList.exception.WorkItemNotFoundException;
import com.springboot.MyTodoList.model.WorkItem;
import com.springboot.MyTodoList.repository.AppUserRepository;
import com.springboot.MyTodoList.repository.SprintRepository;
import com.springboot.MyTodoList.repository.WorkItemRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WorkItemService {
    private final WorkItemRepository workItemRepository;
    private final AppUserRepository appUserRepository;
    private final SprintRepository sprintRepository;
    private final WorkItemAssignmentService assignmentService;
    private static final Logger log = LoggerFactory.getLogger(WorkItemService.class);
    private final AppUserRepository userRepository;

    public WorkItemService(WorkItemRepository repository, 
                           AppUserRepository appUserRepository,
                           SprintRepository sprintRepository,
                           WorkItemAssignmentService assignmentService,
                           AppUserRepository userRepository) {
        this.workItemRepository = repository;
        this.appUserRepository = appUserRepository;
        this.sprintRepository = sprintRepository;
        this.assignmentService = assignmentService;
        this.userRepository = userRepository;
    }
    
    public List<WorkItemResponse> findAll() {
        return workItemRepository
                .findAll()
                .stream()
                .map(WorkItemMapper::toResponse)
                .toList();
    }
    
    public WorkItemResponse findById(String id) {
        return workItemRepository
                .findById(id)
                .map(WorkItemMapper::toResponse)
                .orElseThrow(() -> new WorkItemNotFoundException(id));
    }
    
    public List<WorkItemResponse> findByTelegramUserId(String userId) {
        ensureUserExistsByTelegramId(userId);
        return workItemRepository
                .findByTelegramUserId(userId)
                .stream()
                .map(WorkItemMapper::toResponse)
                .toList();
    }
    
    @Transactional
    public WorkItemResponse createWorkItem(CreateWorkItemRequest request) {
        validateCreateWorkItem(request);
        
        WorkItem workItem = WorkItemMapper.fromCreateRequest(request);
        WorkItem saved = workItemRepository.save(workItem);
        if (request.getAssigneeIds() != null) {
            assignmentService.replaceAssignees(saved, request.getAssigneeIds());
        }

        return WorkItemMapper.toResponse(saved);
    }
    
    @Transactional
    public WorkItemResponse updateWorkItem(String id, UpdateWorkItemRequest request) {
        WorkItem workItem = workItemRepository
                .findById(id)
                .orElseThrow(() -> new WorkItemNotFoundException(id));
        validateUpdateWorkItem(request, workItem);
        
        // Applies the non-null attributes of the request to the workItem
        WorkItemMapper.applyUpdates(workItem, request);
        WorkItem savedWorkItem = workItemRepository.save(workItem);
        if (request.getAssigneeIds() != null) {
            assignmentService.replaceAssignees(savedWorkItem, request.getAssigneeIds());
        }

        log.info("Updated work item id={}", savedWorkItem.getWorkItemId());

        return WorkItemMapper.toResponse(savedWorkItem);
    }
    
    @Transactional
    public void deleteWorkItemById(String id) {
        ensureWorkItemExistsById(id);
        workItemRepository.deleteById(id);
    }

    private void validateCreateWorkItem(CreateWorkItemRequest request) {
        if (!appUserRepository.existsById(request.getCreatedByUserId())) {
            log.warn("User does not exist: {}", request.getCreatedByUserId());
            throw new BusinessRuleException("Creator user does not exist: " + request.getCreatedByUserId());
        }

        if (!sprintRepository.existsById(request.getSprintId())) {
            log.warn("Sprint does not exist: {}", request.getSprintId());
            throw new BusinessRuleException("Sprint does not exist: " + request.getSprintId());
        }

        if (request.getEstimatedMinutes() != null &&
                request.getEstimatedMinutes() <= 0) {
            log.warn("Provided invalid estimated minutes: {}", request.getEstimatedMinutes());
            throw new BusinessRuleException("Estimated minutes must be greater than zero");
        }
    }

    private void validateUpdateWorkItem(UpdateWorkItemRequest request, WorkItem existingWorkItem) {
        if (request.getTitle() != null && request.getTitle().isBlank()) {
            throw new BusinessRuleException("Title cannot be blank");
        }

        if (request.getWorkType() != null && request.getWorkType().isBlank()) {
            throw new BusinessRuleException("Work type cannot be blank");
        }

        if (request.getStatus() != null && request.getStatus().isBlank()) {
            throw new BusinessRuleException("Status cannot be blank");
        }

        if (request.getEstimatedMinutes() != null && request.getEstimatedMinutes() < 0) {
            throw new BusinessRuleException("Estimated minutes cannot be negative");
        }

        if (existingWorkItem.getCompletedAt() != null) {
            throw new BusinessRuleException("Completed work items cannot be updated");
        }
    }
    private void ensureWorkItemExistsById(String id) {
        if (!workItemRepository.existsById(id)) {
            log.warn("Work item not found: {}", id);
            throw new WorkItemNotFoundException(id);
        }
    }
    
    private void ensureUserExistsByTelegramId(String id) {
        if (userRepository.findByTelegramUserId(id).isEmpty()) {
            log.warn("User with telegram id {} not found", id);
            throw new AppUserNotFoundException("telegram::"+id);
        }
    }
}
