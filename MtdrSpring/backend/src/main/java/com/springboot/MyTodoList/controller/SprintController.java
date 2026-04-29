package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.dto.sprint.SprintResponse;
import com.springboot.MyTodoList.service.SprintService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sprints")
public class SprintController {
    private final SprintService sprintService;
    
    public SprintController(SprintService sprintService) { this.sprintService = sprintService; }
    
    @GetMapping
    public ResponseEntity<List<SprintResponse>> getAll() {
        return ResponseEntity.ok(sprintService.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<SprintResponse> getSprint(@PathVariable String id) {
        return ResponseEntity.ok(sprintService.findById(id));
    }
}
