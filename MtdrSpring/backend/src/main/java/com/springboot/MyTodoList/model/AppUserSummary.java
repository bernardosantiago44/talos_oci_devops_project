package com.springboot.MyTodoList.model;

public record AppUserSummary(
        String userId,
        String name,
        String email,
        String telegramUserId
) {
}
