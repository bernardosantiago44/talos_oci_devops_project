package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.exception.SprintNotFoundException;
import com.springboot.MyTodoList.exception.WorkItemNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(SprintNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleSprintNotFound(SprintNotFoundException exception) {
        return this.handleGenericNotFound(exception);
    }
    
    @ExceptionHandler(WorkItemNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleWorkItemNotFound(WorkItemNotFoundException exception) {
        return this.handleGenericNotFound(exception);
    }
    
    private ResponseEntity<Map<String, String>> handleGenericNotFound(RuntimeException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", exception.getMessage()));
    }
}
