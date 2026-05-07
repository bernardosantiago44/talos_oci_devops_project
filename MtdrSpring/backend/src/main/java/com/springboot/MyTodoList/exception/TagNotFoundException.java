package com.springboot.MyTodoList.exception;

public class TagNotFoundException extends RuntimeException {
    public TagNotFoundException(String id) {
        super("Tag with id " + id + " not found.");
    }
}
