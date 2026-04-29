package com.springboot.MyTodoList.dto.WorkItem;

public record AssignedUserDto(
        String userId,
        String name,
        String email,
        String telegramUserId
) {}
