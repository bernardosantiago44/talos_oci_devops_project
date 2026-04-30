package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.dto.timeEntry.TimeEntryRequest;
import com.springboot.MyTodoList.dto.timeEntry.TimeEntryResponse;
import com.springboot.MyTodoList.service.TimeEntryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/time-entries")
public class TimeEntryController {
    private final TimeEntryService service;
    
    public TimeEntryController(TimeEntryService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TimeEntryResponse> logTime(
            @Valid @RequestBody 
            TimeEntryRequest request
    ) {
        return ResponseEntity.ok(service.logTime(request));
    }
}
