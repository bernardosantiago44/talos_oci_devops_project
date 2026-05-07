package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.dto.WorkItem.CreateWorkItemRequest;
import com.springboot.MyTodoList.dto.WorkItem.UpdateWorkItemRequest;
import com.springboot.MyTodoList.dto.WorkItem.WorkItemAssignmentDto;
import com.springboot.MyTodoList.dto.WorkItem.WorkItemResponse;
import com.springboot.MyTodoList.service.WorkItemAssignmentService;
import com.springboot.MyTodoList.service.WorkItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/workitems")
@Tag(name = "Work Items", description = "Work item creation, updates, assignment, and lookup endpoints.")
public class WorkItemController {
    private final WorkItemService service;
    private final WorkItemAssignmentService assignmentService;
    
    public WorkItemController(WorkItemService service, WorkItemAssignmentService assignmentService) {
        this.service = service;
        this.assignmentService = assignmentService;
    }

    /**
     * Get all the work items in the database
     * @return List of WorkItemResponse
     * @apiNote /workitems
     */
    @GetMapping
    @Operation(summary = "List work items", description = "Returns all work items with assignment details.")
    public ResponseEntity<List<WorkItemResponse>> getAllWorkItems() {
        return ResponseEntity.ok(service.findAll());
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get work item by ID", description = "Returns a work item by its identifier.")
    public ResponseEntity<WorkItemResponse> getById(
            @Parameter(description = "Work item identifier.", example = "wi-1")
            @PathVariable String id
    ) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    /**
     * Returns the tasks assigned to the provided telegram user id
     * @param telegramUserId String 
     * @return List of WorkItemResponse 
     * @apiNote /workitems/user/{telegramUserId}
     */
    @GetMapping("/telegramUser/{telegramUserId}")
    @Operation(summary = "List work items for Telegram user", description = "Returns work items assigned to the app user with the provided Telegram user ID.")
    public ResponseEntity<List<WorkItemResponse>> getWorkItemsByTelegramUser(
            @Parameter(description = "Telegram user identifier.", example = "telegram-user-1")
            @PathVariable String telegramUserId
    ) {
        return ResponseEntity.ok(service.findByTelegramUserId(telegramUserId));
    }

    /**
     * Create and assign a WorkItem to a user.
     * @param request Object parameters' to create the WorkItem
     * @return ResponseEntity with the WorkItemResponse in body.
     */
    @PostMapping
    @Operation(summary = "Create work item", description = "Creates a work item and optionally assigns users.")
    public ResponseEntity<WorkItemResponse> createWorkItem(
            @Valid @RequestBody 
            CreateWorkItemRequest request
    ) {
        WorkItemResponse createdWorkItem = service.createWorkItem(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdWorkItem);
    }
    
    @PatchMapping("/{id}")
    @Operation(summary = "Update work item", description = "Applies non-null updates to a work item.")
    public ResponseEntity<WorkItemResponse> updateWorkItem(
            @Parameter(description = "Work item identifier.", example = "wi-1")
            @PathVariable String id,
            @Valid @RequestBody UpdateWorkItemRequest request
    ) {
        WorkItemResponse response = service.updateWorkItem(id, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/assignees")
    @Operation(summary = "List work item assignees", description = "Returns active assignees for a work item.")
    public ResponseEntity<List<WorkItemAssignmentDto>> getAssignees(
            @Parameter(description = "Work item identifier.", example = "wi-1")
            @PathVariable String id
    ) {
        return ResponseEntity.ok(assignmentService.getAssignees(id));
    }

    @PatchMapping("/{id}/assignees/{userId}")
    @Operation(summary = "Add work item assignee", description = "Assigns a user to a work item.")
    public ResponseEntity<WorkItemAssignmentDto> addAssignee(
            @Parameter(description = "Work item identifier.", example = "wi-1")
            @PathVariable String id,
            @Parameter(description = "User identifier.", example = "user-1")
            @PathVariable String userId
    ) {
        WorkItemAssignmentDto assignment = assignmentService.addAssignee(id, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(assignment);
    }

    @DeleteMapping("/{id}/assignees/{userId}")
    @Operation(summary = "Remove work item assignee", description = "Removes a user's assignment from a work item.")
    public ResponseEntity<Void> removeAssignee(
            @Parameter(description = "Work item identifier.", example = "wi-1")
            @PathVariable String id,
            @Parameter(description = "User identifier.", example = "user-1")
            @PathVariable String userId
    ) {
        assignmentService.removeAssignee(id, userId);
        return ResponseEntity.noContent().build();
    }

    // DELETE /workitems/{id} — eliminar task
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete work item", description = "Deletes a work item by its identifier.")
    public ResponseEntity<Void> deleteWorkItem(
            @Parameter(description = "Work item identifier.", example = "wi-1")
            @PathVariable String id
    ) {
        service.deleteWorkItemById(id);
        return ResponseEntity.noContent().build();
    }
}
