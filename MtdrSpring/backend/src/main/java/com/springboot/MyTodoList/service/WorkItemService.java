package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.WorkItem.*;
import com.springboot.MyTodoList.exception.AppUserNotFoundException;
import com.springboot.MyTodoList.exception.BusinessRuleException;
import com.springboot.MyTodoList.exception.WorkItemNotFoundException;
import com.springboot.MyTodoList.model.WorkItem;
import com.springboot.MyTodoList.model.WorkItemAssignment;
import com.springboot.MyTodoList.model.WorkItemPriority;
import com.springboot.MyTodoList.query.WorkItemQuery;
import com.springboot.MyTodoList.repository.AppUserRepository;
import com.springboot.MyTodoList.repository.SprintRepository;
import com.springboot.MyTodoList.repository.WorkItemRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class WorkItemService {
    private static final Set<String> VALID_STATUSES = Set.of("NEW", "TODO", "IN_PROGRESS", "BLOCKED", "DONE");
    private static final Set<String> VALID_WORK_TYPES = Set.of("FEATURE", "ISSUE", "BUG", "TASK");
    private final WorkItemRepository workItemRepository;
    private final AppUserRepository appUserRepository;
    private final SprintRepository sprintRepository;
    private final WorkItemAssignmentService assignmentService;
    private final WorkItemTagAssignmentService tagAssignmentService;
    private static final Logger log = LoggerFactory.getLogger(WorkItemService.class);
    private final AppUserRepository userRepository;

    public WorkItemService(WorkItemRepository repository,
                           WorkItemTagAssignmentService tagAssignmentService,
                           AppUserRepository appUserRepository,
                           SprintRepository sprintRepository,
                           WorkItemAssignmentService assignmentService,
                           AppUserRepository userRepository) {
        this.workItemRepository = repository;
        this.appUserRepository = appUserRepository;
        this.sprintRepository = sprintRepository;
        this.assignmentService = assignmentService;
        this.tagAssignmentService = tagAssignmentService;
        this.userRepository = userRepository;
    }
    
    public List<WorkItemResponse> findAll() {
        return workItemRepository
                .findAll()
                .stream()
                .map(WorkItemMapper::toResponse)
                .toList();
    }
    
    public List<WorkItemResponse> findByQuery(@NotNull WorkItemQuery query) {
        WorkItemPriority priority = null;
        if (hasText(query.getPriority())) {
            priority = parsePriority(query.getPriority());
            if (priority == null) {
                return List.of();
            }
        }

        List<String> statuses = normalizeStatuses(query.getStatus());
        if (statuses == null) {
            return List.of();
        }

        String workType = null;
        if (hasText(query.getWorkType())) {
            workType = normalizeUpperSnake(query.getWorkType());
            if (!VALID_WORK_TYPES.contains(workType)) {
                return List.of();
            }
        }

        return workItemRepository
                .findAll(toSpecification(query, priority, statuses, workType))
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
        
        if (request.getTagIds() != null) {
            tagAssignmentService.replaceTags(saved, request.getTagIds());
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
        
        if (request.getTagIds() != null) {
            tagAssignmentService.replaceTags(savedWorkItem, request.getTagIds());
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

    private Specification<WorkItem> toSpecification(
            WorkItemQuery query,
            WorkItemPriority priority,
            List<String> statuses,
            String workType
    ) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (!statuses.isEmpty()) {
                predicates.add(criteriaBuilder.upper(root.get("status")).in(statuses));
            }

            List<String> sprintIds = normalizeIdValues(query.getSprints());
            if (hasText(query.getSprintId())) {
                sprintIds.add(query.getSprintId().trim());
            }
            if (!sprintIds.isEmpty()) {
                predicates.add(root.get("sprintId").in(sprintIds));
            }

            List<String> assigneeIds = normalizeIdValues(query.getAssignees());
            if (!assigneeIds.isEmpty()) {
                assert criteriaQuery != null;
                Subquery<String> assigneeSubquery = criteriaQuery.subquery(String.class);
                Root<WorkItemAssignment> assignment = assigneeSubquery.from(WorkItemAssignment.class);
                assigneeSubquery
                        .select(assignment.get("workItem").get("workItemId"))
                        .where(
                                criteriaBuilder.equal(
                                        assignment.get("workItem").get("workItemId"),
                                        root.get("workItemId")
                                ),
                                assignment.get("assignedUser").get("userId").in(assigneeIds),
                                criteriaBuilder.isNull(assignment.get("unassignedAt"))
                        );

                predicates.add(criteriaBuilder.exists(assigneeSubquery));
            }

            if (workType != null) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.upper(root.get("workType")),
                        workType
                ));
            }

            if (priority != null) {
                predicates.add(criteriaBuilder.equal(root.get("priority"), priority));
            }

            if (hasText(query.getSearch())) {
                String pattern = "%" + escapeLike(query.getSearch().trim().toLowerCase(Locale.ROOT)) + "%";
                Predicate titleContainsSearch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        pattern,
                        '\\'
                );
                Predicate descriptionContainsSearch = criteriaBuilder.and(
                        criteriaBuilder.isNotNull(root.get("description")),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("description")),
                                pattern,
                                '\\'
                        )
                );
                predicates.add(criteriaBuilder.or(titleContainsSearch, descriptionContainsSearch));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private WorkItemPriority parsePriority(String priority) {
        try {
            return WorkItemPriority.valueOf(normalizeUpperSnake(priority));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private List<String> normalizeStatuses(List<String> values) {
        if (values == null) {
            return new ArrayList<>();
        }

        List<String> statuses = values
                .stream()
                .filter(this::hasText)
                .map(this::normalizeUpperSnake)
                .distinct()
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        if (!VALID_STATUSES.containsAll(statuses)) {
            return null;
        }

        return statuses
                .stream()
                .map(status -> status.equals("TODO") ? "NEW" : status)
                .distinct()
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    private List<String> normalizeIdValues(List<String> values) {
        if (values == null) {
            return new ArrayList<>();
        }

        return values
                .stream()
                .filter(this::hasText)
                .map(String::trim)
                .distinct()
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    private String normalizeUpperSnake(String value) {
        return value
                .trim()
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .replaceAll("[\\s-]+", "_")
                .toUpperCase(Locale.ROOT);
    }

    private String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
