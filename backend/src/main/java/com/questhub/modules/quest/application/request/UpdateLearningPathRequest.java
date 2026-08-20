package com.questhub.modules.quest.application.request;

import com.questhub.modules.quest.domain.quest.Difficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateLearningPathRequest(
    @NotBlank @Size(max = 100) String title,
    @Size(max = 1000) String description,
    @NotNull Difficulty difficulty,
    boolean isPublic) {}