package com.springboot.MyTodoList.exception;

public class WorkItemNotFoundException extends RuntimeException {
    public WorkItemNotFoundException(String id) {
        super("Work Item not found: " + id);
    }
}
