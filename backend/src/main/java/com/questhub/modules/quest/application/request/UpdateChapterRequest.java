package com.questhub.modules.quest.application.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateChapterRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 1000) String description,
        @Min(0) Integer position
) {}