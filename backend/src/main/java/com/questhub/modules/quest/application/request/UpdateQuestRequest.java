package com.questhub.modules.quest.application.request;

import com.questhub.modules.quest.domain.quest.CompletionRule;
import com.questhub.modules.quest.domain.quest.Difficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;

public record UpdateQuestRequest(
    @NotBlank @Size(max = 120) String title,
    @Size(max = 2000) String description,
    @NotNull Difficulty difficulty,
    CompletionRule completionRule,
    Map<String, Object> reward) {}