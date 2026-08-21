package com.questhub.modules.quest.application.usecase;

import com.questhub.modules.identity.application.query.GetUsernameQuery;
import com.questhub.modules.quest.domain.personalquest.PersonalQuest;
import com.questhub.modules.quest.domain.personalquest.PersonalQuestRepository;
import com.questhub.modules.quest.domain.quest.Quest;
import com.questhub.modules.quest.domain.quest.QuestRepository;
import com.questhub.modules.quest.domain.quest.QuestVisibility;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
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
public class ForkQuestUseCase {

  private final QuestRepository questRepository;
  private final PersonalQuestRepository personalQuestRepository;
  private final GetUsernameQuery getUsernameQuery;
  private final OutboxPublisher outboxPublisher;

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public PersonalQuest fork(UUID questId, UUID userId) {
    Quest quest =
        questRepository
            .findById(questId)
            .orElseThrow(
                () -> BusinessException.notFound(ErrorCodes.NOT_FOUND, "Không tìm thấy quest"));
    if (quest.getVisibility() != QuestVisibility.PUBLIC) {
      log.warn("Cannot fork non-public quest questId={} visibility={}", questId, quest.getVisibility());
      throw BusinessException.forbidden(
          ErrorCodes.FORBIDDEN, "Chỉ có thể fork quest đã publish");
    }
    if (personalQuestRepository.existsByUserIdAndQuestId(userId, questId)) {
      log.warn("Quest already forked questId={} userId={}", questId, userId);
      throw BusinessException.conflict(ErrorCodes.CONFLICT, "Bạn đã fork quest này rồi");
    }

    PersonalQuest personalQuest = personalQuestRepository.save(quest.forkTo(userId));
    log.info(
        "Quest forked personalQuestId={} questId={} userId={}",
        personalQuest.getId(), questId, userId);

    publishEvent(quest, personalQuest, userId);
    return personalQuest;
  }

  private void publishEvent(Quest quest, PersonalQuest personalQuest, UUID userId) {
    String username = getUsernameQuery.byUserId(userId).orElse(null);

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("questId", quest.getId().toString());
    payload.put("questTitle", quest.getTitle());
    payload.put(
        "learningPathId",
        quest.getLearningPathId() != null ? quest.getLearningPathId().toString() : null);
    payload.put("personalQuestId", personalQuest.getId().toString());
    payload.put("userId", userId.toString());
    payload.put("username", username);

    outboxPublisher.publish("Quest", quest.getId(), "quest.forked", payload);
  }
}

