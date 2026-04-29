package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.WorkItem.CreateWorkItemRequest;
import com.springboot.MyTodoList.dto.WorkItem.WorkItemMapper;
import com.springboot.MyTodoList.dto.WorkItem.WorkItemResponse;
import com.springboot.MyTodoList.exception.BusinessRuleException;
import com.springboot.MyTodoList.exception.WorkItemNotFoundException;
import com.springboot.MyTodoList.model.WorkItem;
import com.springboot.MyTodoList.repository.SprintRepository;
import com.springboot.MyTodoList.repository.UserRepository;
import com.springboot.MyTodoList.repository.WorkItemRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WorkItemService {
    private final WorkItemRepository workItemRepository;
    private final UserRepository userRepository;
    private final SprintRepository sprintRepository;
    private static final Logger log = LoggerFactory.getLogger(WorkItemService.class);
    
    public WorkItemService(WorkItemRepository repository, 
                           UserRepository userRepository, 
                           SprintRepository sprintRepository
    ) {
        this.workItemRepository = repository;
        this.userRepository = userRepository;
        this.sprintRepository = sprintRepository;
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
    
    public List<WorkItemResponse> findForUserId(String userId) {
        return workItemRepository
                .findForUserId(userId)
                .stream()
                .map(WorkItemMapper::toResponse)
                .toList();
    }
    
    @Transactional
    public WorkItemResponse createWorkItem(CreateWorkItemRequest request) {
        validateCreateWorkItem(request);
        
        WorkItem workItem = WorkItemMapper.fromCreateRequest(request);
        WorkItem saved = workItemRepository.save(workItem);
        return WorkItemMapper.toResponse(saved);
    }

    private void validateCreateWorkItem(CreateWorkItemRequest request) {
        if (!userRepository.existsById(request.getCreatedByUserId())) {
            log.warn("User does not exist: {}", request.getCreatedByUserId());
            throw new BusinessRuleException("Creator user does not exist: " + request.getCreatedByUserId());
        }

        if (request.getSprintId() != null && !sprintRepository.existsById(request.getSprintId())) {
            log.warn("Sprint does not exist: {}", request.getSprintId());
            throw new BusinessRuleException("Sprint does not exist: " + request.getSprintId());
        }

        if (request.getEstimatedMinutes() != null &&
                request.getEstimatedMinutes() <= 0) {
            log.warn("Provided invalid estimated minutes: {}", request.getEstimatedMinutes());
            throw new BusinessRuleException("Estimated minutes must be greater than zero");
        }
    }
}
