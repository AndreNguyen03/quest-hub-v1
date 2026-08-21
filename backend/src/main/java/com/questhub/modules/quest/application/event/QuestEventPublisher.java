package com.questhub.modules.quest.application.event;

import com.questhub.modules.quest.domain.learningpath.LearningPathRepository;
import com.questhub.modules.quest.domain.personalquest.PersonalQuest;
import com.questhub.shared.outbox.OutboxPublisher;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestEventPublisher {

  private final LearningPathRepository learningPathRepository;
  private final OutboxPublisher outboxPublisher;

  public void publishCompleted(PersonalQuest personalQuest, UUID userId) {
    String questId = null;
    if (personalQuest.getQuestId() != null) {
      questId = personalQuest.getQuestId().toString();
    }

    String learningPathId = null;
    if (personalQuest.getLearningPathId() != null) {
      learningPathId = personalQuest.getLearningPathId().toString();
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
    payload.put("personalQuestId", personalQuest.getId().toString());
    payload.put("questId", questId);
    payload.put("questTitle", personalQuest.getTitle());
    payload.put("learningPathId", learningPathId);
    payload.put("skillDomainId", skillDomainId);
    payload.put("completedAt", personalQuest.getCompletedAt().toString());

    outboxPublisher.publish("Quest", personalQuest.getId(), "quest.completed", payload);
  }

  public void publishReopened(PersonalQuest personalQuest, UUID userId) {
    String learningPathId = null;
    if (personalQuest.getLearningPathId() != null) {
      learningPathId = personalQuest.getLearningPathId().toString();
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
    payload.put("personalQuestId", personalQuest.getId().toString());
    payload.put("questId", personalQuest.getQuestId() != null ? personalQuest.getQuestId().toString() : null);
    payload.put("questTitle", personalQuest.getTitle());
    payload.put("learningPathId", learningPathId);
    payload.put("skillDomainId", skillDomainId);

    outboxPublisher.publish("Quest", personalQuest.getId(), "quest.reopened", payload);
  }
}
