package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.exception.BusinessRuleException;
import com.springboot.MyTodoList.exception.SprintNotFoundException;
import com.springboot.MyTodoList.exception.WorkItemNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
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
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex
    ) {
        Map<String, String> fieldErrors = new HashMap<>();

        ex.getBindingResult()
            .getFieldErrors()
            .forEach(error ->
                    fieldErrors.put(error.getField(), error.getDefaultMessage())
            );

        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("error", "Validation Failed");
        response.put("fields", fieldErrors);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessRuleException(
            BusinessRuleException ex
    ) {
        return ResponseEntity
                .badRequest()
                .body(Map.of(
                        "status", 400,
                        "error", "BUSINESS_RULE_VIOLATION",
                        "message", ex.getMessage()
                ));
    }
}
