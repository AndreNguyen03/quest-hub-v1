package com.questhub.modules.quest.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddChapterCommand(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 1000) String description
) {}

