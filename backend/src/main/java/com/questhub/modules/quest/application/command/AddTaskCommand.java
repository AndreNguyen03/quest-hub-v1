package com.questhub.modules.quest.application.command;

import com.questhub.modules.quest.domain.task.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record AddTaskCommand(
        @NotNull TaskType type,
        @NotBlank @Size(max = 200) String title,
        @Size(max = 1000) String description,
        Map<String, Object> config
) {}



