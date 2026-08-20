package com.questhub.modules.quest.application.usecase;

import com.questhub.modules.quest.application.request.UpdateQuestRequest;
import com.questhub.modules.quest.domain.quest.Quest;
import com.questhub.modules.quest.domain.quest.QuestRepository;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class UpdateQuestUseCase {

  private final QuestRepository questRepository;

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public Quest update(UUID questId, UUID actorId, UpdateQuestRequest request) {
    Quest quest =
        questRepository
            .findById(questId)
            .orElseThrow(
                () -> BusinessException.notFound(ErrorCodes.NOT_FOUND, "Không tìm thấy quest"));
    if (!quest.getCreatorId().equals(actorId)) {
      throw BusinessException.forbidden(ErrorCodes.FORBIDDEN, "Chỉ creator mới sửa được quest");
    }
    quest.updateMetadata(
        request.title(),
        request.description(),
        request.difficulty(),
        request.completionRule(),
        request.reward());
    Quest saved = questRepository.save(quest);
    log.info("Quest updated questId={} actorId={}", questId, actorId);
    return saved;
  }
}