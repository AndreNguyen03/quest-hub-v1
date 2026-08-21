package com.questhub.modules.identity.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileCommand(
    @Size(max = 255) String avatarUrl,
    @Size(max = 300) String bio,
    @NotBlank @Size(max = 100) String displayName,
    boolean isPublic) {}
