package com.springboot.MyTodoList.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request payload for updating the authenticated user's profile.")
public class UpdateProfileRequest {
    @Size(max = 255)
    @Schema(description = "Updated display name.", example = "Updated Name")
    private String name;

    @Size(max = 255)
    @Schema(description = "Updated Telegram user identifier.", example = "123456")
    private String telegramUserId;

    @Size(max = 255)
    @Schema(description = "Updated phone number.", example = "3312345678")
    private String phoneNumber;

    @Size(min = 8, max = 128)
    @Schema(description = "New plain password. Stored only as a BCrypt hash.", example = "newPassword")
    private String password;
}
