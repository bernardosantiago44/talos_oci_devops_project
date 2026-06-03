package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.dto.sprint.SprintCreateRequest;
import com.springboot.MyTodoList.dto.sprint.SprintResponse;
import com.springboot.MyTodoList.service.SprintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sprints")
@Tag(name = "Sprints", description = "Sprint lookup endpoints.")
public class SprintController {
    private final SprintService sprintService;
    
    public SprintController(SprintService sprintService) { this.sprintService = sprintService; }
    
    @GetMapping
    @Operation(summary = "List sprints", description = "Returns all configured sprints.")
    public ResponseEntity<List<SprintResponse>> getAll() {
        return ResponseEntity.ok(sprintService.findAll());
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get sprint by ID", description = "Returns one sprint by its identifier.")
    public ResponseEntity<SprintResponse> getSprint(
            @Parameter(description = "Sprint identifier.", example = "sprint-1")
            @PathVariable String id
    ) {
        return ResponseEntity.ok(sprintService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create sprint", description = "Creates a new sprint.")
    public ResponseEntity<SprintResponse> createSprint(@RequestBody SprintCreateRequest request) {
        SprintResponse created = sprintService.createSprint(request);
        return ResponseEntity.created(URI.create("/api/sprints/" + created.sprintId())).body(created);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update sprint status", description = "Sets the status of a sprint (ACTIVE, INACTIVE, COMPLETED).")
    public ResponseEntity<SprintResponse> updateStatus(
            @Parameter(description = "Sprint identifier.", example = "sprint-1")
            @PathVariable String id,
            @RequestBody Map<String, String> body
    ) {
        return ResponseEntity.ok(sprintService.updateStatus(id, body.get("status")));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete sprint", description = "Deletes a sprint by its identifier.")
    public ResponseEntity<Void> deleteSprint(
            @Parameter(description = "Sprint identifier.", example = "sprint-1")
            @PathVariable String id
    ) {
        sprintService.deleteSprint(id);
        return ResponseEntity.noContent().build();
    }
}
