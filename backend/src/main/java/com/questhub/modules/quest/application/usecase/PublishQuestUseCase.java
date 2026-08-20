package com.questhub.modules.quest.application.usecase;

import com.questhub.modules.identity.domain.user.Role;
import com.questhub.modules.identity.domain.user.UserRepository;
import com.questhub.modules.quest.application.helper.QuestAcess;
import com.questhub.modules.quest.domain.learningpath.LearningPath;
import com.questhub.modules.quest.domain.learningpath.LearningPathRepository;
import com.questhub.modules.quest.domain.quest.Quest;
import com.questhub.modules.quest.domain.quest.QuestRepository;
import com.questhub.modules.quest.domain.quest.QuestVisibility;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.outbox.OutboxPublisher;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class PublishQuestUseCase {

  private final QuestAcess questAcess;
  private final QuestRepository questRepository;
  private final LearningPathRepository learningPathRepository;
  private final UserRepository userRepository;
  private final OutboxPublisher outboxPublisher;

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public Quest publish(UUID questId, UUID creatorId) {
    Quest quest = questAcess.loadForWrite(questId, creatorId);
    if (quest.getVisibility() == QuestVisibility.PUBLIC) {
      log.info("Quest already public questId={} creatorId={}", questId, creatorId);
      return quest;
    }

    boolean firstPublish =
        !questRepository.existsByCreatorIdAndVisibility(creatorId, QuestVisibility.PUBLIC);
    quest.publish();

    Quest saved = questRepository.save(quest);
    log.info("Quest published questId={} creatorId={} firstPublish={}",
        saved.getId(), creatorId, firstPublish);

    publishEvent(saved, creatorId);
    if (firstPublish) {
      promoteToCreator(creatorId);
    }
    return saved;
  }

  private void publishEvent(Quest quest, UUID creatorId) {
    UUID skillDomainId = null;
    if (quest.getLearningPathId() != null) {
      skillDomainId =
          learningPathRepository
              .findById(quest.getLearningPathId())
              .map(LearningPath::getDomainId)
              .orElse(null);
    }

    String creatorUsername =
        userRepository
            .findById(creatorId)
            .map(user -> user.getUsername().value())
            .orElse(null);

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("questId", quest.getId().toString());
    payload.put(
        "learningPathId",
        quest.getLearningPathId() != null ? quest.getLearningPathId().toString() : null);
    payload.put("title", quest.getTitle());
    payload.put("description", quest.getDescription());
    payload.put("skillDomainId", skillDomainId != null ? skillDomainId.toString() : null);
    payload.put("difficulty", quest.getDifficulty().name());
    payload.put(
        "taskCount",
        quest.getChapters().stream().mapToInt(chapter -> chapter.getTasks().size()).sum());
    payload.put("creatorId", creatorId.toString());
    payload.put("creatorUsername", creatorUsername);
    payload.put("publishedAt", quest.getPublishedAt().toString());

    outboxPublisher.publish("Quest", quest.getId(), "quest.published", payload);
  }

  private void promoteToCreator(UUID creatorId) {
    userRepository.findById(creatorId).ifPresent(user -> {
      if (user.getRole() == Role.USER) {
        user.promoteToCreator();
        userRepository.save(user);
        log.info("User promoted to CREATOR userId={}", creatorId);
      }
    });
  }
}