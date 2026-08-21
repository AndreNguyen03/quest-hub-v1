package com.questhub.modules.marketplace.application.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateReviewCommand(
    @NotNull @Size(min = 1, max = 5) Integer score,
    @Size(max = 2000) String content) {}
