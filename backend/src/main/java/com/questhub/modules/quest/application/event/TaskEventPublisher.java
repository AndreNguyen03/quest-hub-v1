package com.questhub.modules.quest.application.event;

import com.questhub.modules.identity.application.api.IdentityPublicApi;
import com.questhub.modules.quest.domain.learningpath.LearningPathRepository;
import com.questhub.modules.quest.domain.personalquest.PersonalChapter;
import com.questhub.modules.quest.domain.personalquest.PersonalQuest;
import com.questhub.modules.quest.domain.personalquest.PersonalTask;
import com.questhub.shared.outbox.OutboxPublisher;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskEventPublisher {

  private final IdentityPublicApi identityPublicApi;
  private final LearningPathRepository learningPathRepository;
  private final OutboxPublisher outboxPublisher;

  public void publishCompleted(PersonalQuest personalQuest, PersonalTask task, UUID userId) {
    Map<String, Object> payload = basePayload(personalQuest, task, userId);
    payload.put("newProgress", personalQuest.getProgress());
    payload.put("isQuestCompleted", personalQuest.isCompleted());
    outboxPublisher.publish("Quest", personalQuest.getId(), "task.completed", payload);
  }

  public void publishUndone(PersonalQuest personalQuest, PersonalTask task, UUID userId) {
    Map<String, Object> payload = basePayload(personalQuest, task, userId);
    payload.put("newProgress", personalQuest.getProgress());
    payload.put("isQuestCompleted", personalQuest.isCompleted());
    payload.put("undoneAt", Instant.now().toString());
    outboxPublisher.publish("Quest", personalQuest.getId(), "task.undone", payload);
  }

  private Map<String, Object> basePayload(
      PersonalQuest personalQuest, PersonalTask task, UUID userId) {
    String username = identityPublicApi.findUsername(userId).orElse(null);
    PersonalChapter chapter =
        personalQuest.findChapterOf(task.getId()).orElse(null);

    String questId = null;
    if (personalQuest.getQuestId() != null) {
      questId = personalQuest.getQuestId().toString();
    }

    String chapterId = null;
    String chapterTitle = null;
    if (chapter != null) {
      chapterTitle = chapter.getTitle();
      if (chapter.getSourceChapterId() != null) {
        chapterId = chapter.getSourceChapterId().toString();
      } else {
        chapterId = chapter.getId().toString();
      }
    }

    String taskId;
    if (task.getSourceTaskId() != null) {
      taskId = task.getSourceTaskId().toString();
    } else {
      taskId = task.getId().toString();
    }

    String skillDomainId = null;
    if (personalQuest.getLearningPathId() != null) {
      skillDomainId =
          learningPathRepository
              .findById(personalQuest.getLearningPathId())
              .map(lp -> lp.getDomainId().toString())
              .orElse(null);
    }

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("userId", userId.toString());
    payload.put("username", username);
    payload.put("personalQuestId", personalQuest.getId().toString());
    payload.put("questId", questId);
    payload.put("questTitle", personalQuest.getTitle());
    payload.put("chapterId", chapterId);
    payload.put("chapterTitle", chapterTitle);
    payload.put("taskId", taskId);
    payload.put("taskTitle", task.getTitle());
    payload.put("taskType", task.getType().name());
    payload.put("skillDomainId", skillDomainId);
    return payload;
  }
}