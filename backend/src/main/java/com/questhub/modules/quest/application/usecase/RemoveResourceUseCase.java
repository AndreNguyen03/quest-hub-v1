package com.questhub.modules.quest.application.usecase;

import com.questhub.modules.quest.application.helper.QuestCreatorGuard;
import com.questhub.modules.quest.domain.quest.Quest;
import com.questhub.modules.quest.domain.quest.QuestRepository;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class RemoveResourceUseCase {
  private final QuestCreatorGuard questCreatorGuard;
  private final QuestRepository questRepository;

  @Transactional(
      isolation = Isolation.DEFAULT,
      propagation = Propagation.REQUIRED,
      rollbackFor = Exception.class)
  public Quest remove(UUID resourceId, UUID actorId) {
    Quest quest =
        questRepository
            .findQuestByResourceId(resourceId)
            .orElseThrow(
                () -> BusinessException.notFound(ErrorCodes.NOT_FOUND, "Không tìm thấy resource"));
    questCreatorGuard.verifyCreator(quest, actorId);
    quest.removeResource(resourceId);
    Quest saved = questRepository.save(quest);
    log.info("Resource removed resourceId={} actorId={}", resourceId, actorId);
    return saved;
  }
}

