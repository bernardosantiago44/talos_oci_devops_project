package com.springboot.MyTodoList.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request payload for creating an app user account.")
public class SignupRequest {
    @NotBlank
    @Size(max = 255)
    @Schema(description = "Display name.", example = "User Name")
    private String name;

    @Email
    @NotBlank
    @Size(max = 255)
    @Schema(description = "Unique account email address.", example = "user@example.com")
    private String email;

    @NotBlank
    @Size(min = 8, max = 128)
    @Schema(description = "Plain account password. Stored only as a BCrypt hash.", example = "plainPassword")
    private String password;
}
