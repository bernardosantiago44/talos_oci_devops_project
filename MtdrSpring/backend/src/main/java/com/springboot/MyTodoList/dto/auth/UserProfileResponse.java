package com.springboot.MyTodoList.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Safe app user profile fields.")
public record UserProfileResponse(
        @Schema(description = "User identifier.", example = "user-1")
        String userId,
        @Schema(description = "Display name.", example = "User Name")
        String name,
        @Schema(description = "Email address.", example = "user@example.com")
        String email,
        @Schema(description = "Telegram user identifier.", example = "123456")
        String telegramUserId,
        @Schema(description = "Phone number.", example = "3312345678")
        String phoneNumber
) {
}
