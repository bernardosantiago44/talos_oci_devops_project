package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.dto.timeEntry.TimeEntryRequest;
import com.springboot.MyTodoList.dto.timeEntry.TimeEntryResponse;
import com.springboot.MyTodoList.service.TimeEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/time-entries")
@Tag(name = "Time Entries", description = "Time logging endpoints for work items.")
public class TimeEntryController {
    private final TimeEntryService service;
    
    public TimeEntryController(TimeEntryService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Log time", description = "Creates a time entry for a user and work item.")
    public ResponseEntity<TimeEntryResponse> logTime(
            @Valid @RequestBody 
            TimeEntryRequest request
    ) {
        return ResponseEntity.ok(service.logTime(request));
    }
}
