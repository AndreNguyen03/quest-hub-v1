package com.questhub.modules.quest.application.command;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record UpdateTaskCommand(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 1000) String description,
        Map<String, Object> config,
        @Min(0) Integer order
) {}
