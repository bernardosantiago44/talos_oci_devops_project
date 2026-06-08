package com.springboot.MyTodoList.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication token and safe user profile.")
public record AuthResponse(
        @Schema(description = "JWT bearer token.")
        String token,
        @Schema(description = "Authenticated user profile.")
        UserProfileResponse user
) {
}
