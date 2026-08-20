package com.questhub.modules.quest.application.request;

import com.questhub.modules.quest.domain.quest.CompletionRule;
import com.questhub.modules.quest.domain.quest.Difficulty;
import com.questhub.modules.quest.domain.quest.TaskType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CreateQuestRequest(
    @NotBlank @Size(max = 120) String title,
    @Size(max = 2000) String description,
    UUID learningPathId,
    @NotNull Difficulty difficulty,
    CompletionRule completionRule,
    Map<String, Object> reward,
    @NotEmpty @Valid List<ChapterRequest> chapters) {

  public record ChapterRequest(
      @NotBlank @Size(max = 200) String title,
      @Size(max = 1000) String description,
      @NotEmpty @Valid List<TaskRequest> tasks) {}

  public record TaskRequest(
      @NotNull TaskType type,
      @NotBlank @Size(max = 200) String title,
      @Size(max = 1000) String description,
      Map<String, Object> config) {}
}