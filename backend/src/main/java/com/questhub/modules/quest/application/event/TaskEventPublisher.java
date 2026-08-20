package com.questhub.modules.quest.application.event;

import com.questhub.modules.identity.domain.user.UserRepository;
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

  private final UserRepository userRepository;
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
    String username =
        userRepository
            .findById(userId)
            .map(user -> user.getUsername().value())
            .orElse(null);
    PersonalChapter chapter =
        personalQuest.findChapterOf(task.getId()).orElse(null);

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("userId", userId.toString());
    payload.put("username", username);
    payload.put("personalQuestId", personalQuest.getId().toString());
    payload.put("questId", personalQuest.getQuestId() != null ? personalQuest.getQuestId().toString() : null);
    payload.put("questTitle", personalQuest.getTitle());
    payload.put(
        "chapterId",
        chapter != null
            ? (chapter.getSourceChapterId() != null
                ? chapter.getSourceChapterId().toString()
                : chapter.getId().toString())
            : null);
    payload.put("chapterTitle", chapter != null ? chapter.getTitle() : null);
    payload.put(
        "taskId",
        task.getSourceTaskId() != null ? task.getSourceTaskId().toString() : task.getId().toString());
    payload.put("taskTitle", task.getTitle());
    payload.put("taskType", task.getType().name());
    payload.put(
        "skillDomainId",
        personalQuest.getLearningPathId() != null
            ? learningPathRepository
                .findById(personalQuest.getLearningPathId())
                .map(lp -> lp.getDomainId().toString())
                .orElse(null)
            : null);
    return payload;
  }
}