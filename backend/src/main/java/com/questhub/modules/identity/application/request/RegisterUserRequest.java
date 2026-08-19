package com.questhub.modules.identity.application.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
    @NotBlank @Email String email,
    @NotBlank
        @Pattern(
            regexp = "^[a-z0-9_]+$",
            message = "Username chỉ gồm chữ thường, số và dấu gạch dưới")
        String username,
    @NotBlank @Size(max = 100) String displayName,
    @NotBlank @Size(min = 8) String password) {}