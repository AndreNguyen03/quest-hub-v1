package com.questhub.modules.quest.application.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddChapterRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 1000) String description
) {}
