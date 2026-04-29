package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.dto.WorkItem.CreateWorkItemRequest;
import com.springboot.MyTodoList.dto.WorkItem.WorkItemResponse;
import com.springboot.MyTodoList.service.WorkItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/workitems")
public class WorkItemController {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private final WorkItemService service;
    
    public WorkItemController(WorkItemService service) {
        this.service = service;
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

    // DELETE /workitems/{id} — eliminar task
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkItem(@PathVariable String id) {
        jdbcTemplate.update("DELETE FROM CHATBOT_USER.WORK_ITEM_ASSIGNMENT WHERE WORK_ITEM_ID = ?", id);
        int rows = jdbcTemplate.update("DELETE FROM CHATBOT_USER.WORK_ITEM WHERE WORK_ITEM_ID = ?", id);
        if (rows == 0) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }

    // PUT /workitems/{id}/status — cambiar status
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable String id,
                                                             @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        if ("TODO".equals(newStatus)) newStatus = "NEW";
        boolean isDone = "DONE".equals(newStatus) || "COMPLETED".equals(newStatus);

        int rows = jdbcTemplate.update(
            "UPDATE CHATBOT_USER.WORK_ITEM SET STATUS = ?, UPDATED_AT = CURRENT_TIMESTAMP, " +
            "COMPLETED_AT = CASE WHEN ? = 1 THEN CURRENT_TIMESTAMP ELSE NULL END " +
            "WHERE WORK_ITEM_ID = ?",
            newStatus, isDone ? 1 : 0, id
        );

        if (rows == 0) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("workItemId", id, "status", newStatus));
    }
}
