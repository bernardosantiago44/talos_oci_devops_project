package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.dto.WorkItem.CreateWorkItemRequest;
import com.springboot.MyTodoList.dto.WorkItem.UpdateWorkItemRequest;
import com.springboot.MyTodoList.dto.WorkItem.WorkItemAssignmentDto;
import com.springboot.MyTodoList.dto.WorkItem.WorkItemResponse;
import com.springboot.MyTodoList.service.WorkItemAssignmentService;
import com.springboot.MyTodoList.service.WorkItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/workitems")
public class WorkItemController {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
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
    public ResponseEntity<List<WorkItemResponse>> getAllWorkItems() {
        return ResponseEntity.ok(service.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<WorkItemResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    /**
     * Returns the tasks assigned to the provided telegram user id
     * @param telegramUserId String 
     * @return List of WorkItemResponse 
     * @apiNote /workitems/user/{telegramUserId}
     */
    @GetMapping("/user/{telegramUserId}")
    public ResponseEntity<List<WorkItemResponse>> getWorkItemsByTelegramUser(@PathVariable String telegramUserId) {
        return ResponseEntity.ok(service.findForUserId(telegramUserId));
    }

    /**
     * Create and assign a WorkItem to a user.
     * @param request Object parameters' to create the WorkItem
     * @return ResponseEntity with the WorkItemResponse in body.
     */
    @PostMapping
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
    public ResponseEntity<WorkItemResponse> updateWorkItem(
            @PathVariable String id,
            @RequestBody UpdateWorkItemRequest request
    ) {
        WorkItemResponse response = service.updateWorkItem(id, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/assignees")
    public ResponseEntity<List<WorkItemAssignmentDto>> getAssignees(@PathVariable String id) {
        return ResponseEntity.ok(assignmentService.getAssignees(id));
    }

    @PostMapping("/{id}/assignees/{userId}")
    public ResponseEntity<WorkItemAssignmentDto> addAssignee(
            @PathVariable String id,
            @PathVariable String userId
    ) {
        WorkItemAssignmentDto assignment = assignmentService.addAssignee(id, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(assignment);
    }

    @DeleteMapping("/{id}/assignees/{userId}")
    public ResponseEntity<Void> removeAssignee(
            @PathVariable String id,
            @PathVariable String userId
    ) {
        assignmentService.removeAssignee(id, userId);
        return ResponseEntity.noContent().build();
    }

    // DELETE /workitems/{id} — eliminar task
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkItem(@PathVariable String id) {
        service.deleteWorkItemById(id);
        return ResponseEntity.noContent().build();
    }
}
