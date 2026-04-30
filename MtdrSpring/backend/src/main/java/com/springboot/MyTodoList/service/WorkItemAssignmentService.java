package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.WorkItem.WorkItemAssignmentDto;
import com.springboot.MyTodoList.dto.WorkItem.WorkItemMapper;
import com.springboot.MyTodoList.exception.BusinessRuleException;
import com.springboot.MyTodoList.exception.WorkItemNotFoundException;
import com.springboot.MyTodoList.model.AppUser;
import com.springboot.MyTodoList.model.WorkItem;
import com.springboot.MyTodoList.model.WorkItemAssignment;
import com.springboot.MyTodoList.repository.AppUserRepository;
import com.springboot.MyTodoList.repository.WorkItemAssignmentRepository;
import com.springboot.MyTodoList.repository.WorkItemRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class WorkItemAssignmentService {
    private static final Logger log = LoggerFactory.getLogger(WorkItemAssignmentService.class);
    private static final String ASSIGNEE_ROLE = "ASSIGNEE";

    private final WorkItemRepository workItemRepository;
    private final AppUserRepository appUserRepository;
    private final WorkItemAssignmentRepository assignmentRepository;

    public WorkItemAssignmentService(WorkItemRepository workItemRepository,
                                     AppUserRepository appUserRepository,
                                     WorkItemAssignmentRepository assignmentRepository) {
        this.workItemRepository = workItemRepository;
        this.appUserRepository = appUserRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional
    public WorkItemAssignmentDto addAssignee(String workItemId, String userId) {
        WorkItem workItem = findWorkItem(workItemId);
        AppUser user = findUser(userId);

        boolean alreadyAssigned = assignmentRepository
                .existsByWorkItem_WorkItemIdAndAssignedUser_UserId(workItemId, userId);
        if (alreadyAssigned) {
            throw new BusinessRuleException("User is already assigned to this work item");
        }

        WorkItemAssignment savedAssignment = saveAssignmentToRepository(workItem, user);
        log.info("Added assignee userId={} to workItemId={}", userId, workItemId);

        return WorkItemMapper.toAssignmentDto(savedAssignment);
    }

    private WorkItemAssignment saveAssignmentToRepository(WorkItem workItem, AppUser user) {
        WorkItemAssignment assignment = new WorkItemAssignment();
        assignment.setAssignmentId(UUID.randomUUID().toString());
        assignment.setWorkItem(workItem);
        assignment.setAssignedUser(user);
        assignment.setAssignmentRole(ASSIGNEE_ROLE);
        assignment.setAssignedAt(OffsetDateTime.now());

        return assignmentRepository.save(assignment);
    }

    @Transactional
    public void removeAssignee(String workItemId, String userId) {
        assignmentRepository
                .findByWorkItem_WorkItemIdAndAssignedUser_UserId(workItemId, userId)
                .ifPresent(assignment -> {
                    assignmentRepository.delete(assignment);
                    log.info("Removed assignee userId={} from workItemId={}", userId, workItemId);
                });
    }

    @Transactional
    public void replaceAssignees(WorkItem workItem, List<String> assigneeIds) {
        validateNoDuplicates(assigneeIds);

        List<AppUser> users = assigneeIds
                .stream()
                .map(this::findUser)
                .toList();

        assignmentRepository.deleteByWorkItem_WorkItemId(workItem.getWorkItemId());
        workItem.getAssignments().clear();

        for (AppUser user : users) {
            WorkItemAssignment savedAssignment = saveAssignmentToRepository(workItem, user);
            workItem.getAssignments().add(savedAssignment);
        }

        log.info("Replaced assignees for workItemId={} count={}", workItem.getWorkItemId(), assigneeIds.size());
    }

    public List<WorkItemAssignmentDto> getAssignees(String workItemId) {
        if (!workItemRepository.existsById(workItemId)) {
            throw new WorkItemNotFoundException(workItemId);
        }

        return assignmentRepository
                .findByWorkItem_WorkItemId(workItemId)
                .stream()
                .map(WorkItemMapper::toAssignmentDto)
                .toList();
    }

    private WorkItem findWorkItem(String workItemId) {
        return workItemRepository
                .findById(workItemId)
                .orElseThrow(() -> new WorkItemNotFoundException(workItemId));
    }

    private AppUser findUser(String userId) {
        return appUserRepository
                .findById(userId)
                .orElseThrow(() -> new BusinessRuleException("User does not exist: " + userId));
    }

    private void validateNoDuplicates(List<String> assigneeIds) {
        Set<String> uniqueIds = new HashSet<>();

        for (String userId : assigneeIds) {
            if (userId == null || userId.isBlank()) {
                throw new BusinessRuleException("Assignee user id is required");
            }

            boolean isNewId = uniqueIds.add(userId);
            if (!isNewId) {
                throw new BusinessRuleException("Duplicate assignees are not allowed");
            }
        }
    }
}
