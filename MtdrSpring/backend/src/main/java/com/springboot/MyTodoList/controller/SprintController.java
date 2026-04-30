package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.dto.sprint.SprintResponse;
import com.springboot.MyTodoList.service.SprintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
