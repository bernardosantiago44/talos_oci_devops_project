package com.springboot.MyTodoList.exception;

public class SprintNotFoundException extends RuntimeException {
    public SprintNotFoundException(String sprintId) {
        super("Sprint not found: " + sprintId);
    }
}
