package com.questhub.modules.quest.application.dto;

import com.questhub.modules.quest.domain.personalquest.PersonalChapter;
import com.questhub.modules.quest.domain.personalquest.PersonalQuest;
import com.questhub.modules.quest.domain.personalquest.PersonalQuestStatus;
import com.questhub.modules.quest.domain.personalquest.PersonalTask;
import com.questhub.modules.quest.domain.quest.CompletionRule;
import com.questhub.modules.quest.domain.task.TaskType;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PersonalQuestResponse(
    UUID id,
    UUID userId,
    UUID questId,
    UUID learningPathId,
    String title,
    CompletionRule completionRule,
    PersonalQuestStatus status,
    int progress,
    List<PersonalChapterResponse> chapters,
    Instant createdAt,
    Instant updatedAt,
    Instant completedAt) {

  public static PersonalQuestResponse from(PersonalQuest personalQuest) {
    return new PersonalQuestResponse(
        personalQuest.getId(),
        personalQuest.getUserId(),
        personalQuest.getQuestId(),
        personalQuest.getLearningPathId(),
        personalQuest.getTitle(),
        personalQuest.getCompletionRule(),
        personalQuest.getStatus(),
        personalQuest.getProgress(),
        personalQuest.getChapters().stream().map(PersonalChapterResponse::from).toList(),
        personalQuest.getCreatedAt(),
        personalQuest.getUpdatedAt(),
        personalQuest.getCompletedAt());
  }

  public record PersonalChapterResponse(
      UUID id,
      UUID sourceChapterId,
      String title,
      String description,
      int position,
      List<PersonalTaskResponse> tasks) {

    public static PersonalChapterResponse from(PersonalChapter chapter) {
      return new PersonalChapterResponse(
          chapter.getId(),
          chapter.getSourceChapterId(),
          chapter.getTitle(),
          chapter.getDescription(),
          chapter.getPosition(),
          chapter.getTasks().stream().map(PersonalTaskResponse::from).toList());
    }
  }

  public record PersonalTaskResponse(
      UUID id,
      UUID sourceTaskId,
      TaskType type,
      String title,
      String description,
      int order,
      Map<String, Object> config,
      boolean isCompleted,
      Instant completedAt) {

    public static PersonalTaskResponse from(PersonalTask task) {
      return new PersonalTaskResponse(
          task.getId(),
          task.getSourceTaskId(),
          task.getType(),
          task.getTitle(),
          task.getDescription(),
          task.getOrder(),
          task.getConfig(),
          task.isCompleted(),
          task.getCompletedAt());
    }
  }
}



