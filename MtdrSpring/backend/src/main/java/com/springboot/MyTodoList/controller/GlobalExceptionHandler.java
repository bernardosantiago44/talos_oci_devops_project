package com.springboot.MyTodoList.controller;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.springboot.MyTodoList.exception.*;
import com.springboot.MyTodoList.model.WorkItemPriority;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AiServiceUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleAiServiceUnavailable(
            AiServiceUnavailableException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", 503,
                        "error", "AI_SERVICE_UNAVAILABLE",
                        "message", ex.getMessage()
                ));
    }
    @ExceptionHandler(SprintNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleSprintNotFound(SprintNotFoundException exception) {
        return this.handleGenericNotFound(exception);
    }

    @ExceptionHandler(AppUserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(AppUserNotFoundException exception) {
        return handleGenericNotFound(exception);
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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadableMessage(
            HttpMessageNotReadableException ex
    ) {
        String message = "Malformed JSON request";

        if (ex.getCause() instanceof InvalidFormatException invalidFormatException &&
                invalidFormatException.getTargetType() == WorkItemPriority.class) {
            message = "Priority must be one of: LOW, MEDIUM, HIGH";
        }

        return ResponseEntity
                .badRequest()
                .body(Map.of(
                        "status", 400,
                        "error", "BAD_REQUEST",
                        "message", message
                ));
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
