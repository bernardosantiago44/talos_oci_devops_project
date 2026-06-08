package com.springboot.MyTodoList.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request payload for signing in with email and password.")
public class LoginRequest {
    @Email
    @NotBlank
    @Schema(description = "Account email address.", example = "user@example.com")
    private String email;

    @NotBlank
    @Schema(description = "Account password.", example = "plainPassword")
    private String password;
}
