package com.questhub.modules.quest.application.request;

import com.questhub.modules.quest.domain.quest.ResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddResourceRequest(
        @NotNull ResourceType type,
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 2000) String url,
        Integer estimatedMinutes
) {}