package com.springboot.MyTodoList.dto.WorkItem;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Compact user details for assignment responses.")
public record AssignedUserDto(
        @Schema(description = "User identifier.", example = "user-1")
        String userId,
        @Schema(description = "Display name.", example = "Ada Lovelace")
        String name,
        @Schema(description = "Email address.", example = "ada@example.com")
        String email,
        @Schema(description = "Telegram user identifier.", example = "telegram-user-1")
        String telegramUserId
) {}
