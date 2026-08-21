package com.questhub.modules.identity.application.command;

import jakarta.validation.constraints.NotBlank;

public record RefreshCommand(@NotBlank String refreshToken) {}
