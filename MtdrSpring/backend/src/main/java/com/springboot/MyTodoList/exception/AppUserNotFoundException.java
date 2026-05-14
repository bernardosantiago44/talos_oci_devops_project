package com.springboot.MyTodoList.exception;

public class AppUserNotFoundException extends RuntimeException {
    public AppUserNotFoundException(String userId) {
        super("App user with id " + userId + " does not exist.");
    }
}
