package com.questhub.modules.quest.application.dto;

import com.questhub.modules.quest.domain.quest.CompletionRule;
import com.questhub.modules.quest.domain.quest.Difficulty;
import com.questhub.modules.quest.domain.quest.Quest;
import com.questhub.modules.quest.domain.quest.QuestVisibility;
import com.questhub.modules.quest.domain.resource.ResourceType;
import com.questhub.modules.quest.domain.task.TaskType;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record QuestResponse(
    UUID id,
    UUID creatorId,
    UUID learningPathId,
    String title,
    String description,
    Difficulty difficulty,
    int estimatedDuration,
    CompletionRule completionRule,
    QuestVisibility visibility,
    Instant publishedAt,
    List<ChapterResponse> chapters,
    Instant createdAt,
    Instant updatedAt) {

  public static QuestResponse from(Quest quest) {
    return new QuestResponse(
        quest.getId(),
        quest.getCreatorId(),
        quest.getLearningPathId(),
        quest.getTitle(),
        quest.getDescription(),
        quest.getDifficulty(),
        quest.getEstimatedDuration(),
        quest.getCompletionRule(),
        quest.getVisibility(),
        quest.getPublishedAt(),
        quest.getChapters().stream().map(ChapterResponse::from).toList(),
        quest.getCreatedAt(),
        quest.getUpdatedAt());
  }

  public record ChapterResponse(
      UUID id,
      String title,
      String description,
      int position,
      List<TaskResponse> tasks) {

    public static ChapterResponse from(com.questhub.modules.quest.domain.chapter.Chapter chapter) {
      return new ChapterResponse(
          chapter.getId(),
          chapter.getTitle(),
          chapter.getDescription(),
          chapter.getPosition(),
          chapter.getTasks().stream().map(TaskResponse::from).toList());
    }
  }

  public record TaskResponse(
      UUID id,
      TaskType type,
      String title,
      String description,
      int order,
      Map<String, Object> config,
      List<ResourceResponse> resources) {

    public static TaskResponse from(com.questhub.modules.quest.domain.task.Task task) {
      return new TaskResponse(
          task.getId(),
          task.getType(),
          task.getTitle(),
          task.getDescription(),
          task.getOrder(),
          task.getConfig(),
          task.getResources().stream().map(ResourceResponse::from).toList());
    }
  }

  public record ResourceResponse(
      UUID id, ResourceType type, String title, String url, Integer estimatedMinutes) {

    public static ResourceResponse from(com.questhub.modules.quest.domain.resource.Resource resource) {
      return new ResourceResponse(
          resource.getId(),
          resource.getType(),
          resource.getTitle(),
          resource.getUrl(),
          resource.getEstimatedMinutes());
    }
  }
}



