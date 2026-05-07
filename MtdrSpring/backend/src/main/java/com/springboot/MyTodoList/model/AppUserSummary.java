package com.springboot.MyTodoList.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Compact app user information.")
public record AppUserSummary(
        @Schema(description = "User identifier.", example = "user-1")
        String userId,
        @Schema(description = "Display name.", example = "Ada Lovelace")
        String name,
        @Schema(description = "Email address.", example = "ada@example.com")
        String email,
        @Schema(description = "Telegram user identifier.", example = "telegram-user-1")
        String telegramUserId
) {
}
