package com.questhub.modules.quest.application.usecase;

import com.questhub.modules.quest.application.helper.QuestAcess;
import com.questhub.modules.quest.domain.quest.Quest;
import com.questhub.modules.quest.domain.quest.QuestRepository;
import com.questhub.modules.quest.domain.quest.QuestVisibility;
import com.questhub.shared.annotation.UseCase;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class UnpublishQuestUseCase {

  private final QuestAcess questAcess;
  private final QuestRepository questRepository;

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public Quest unpublish(UUID questId, UUID creatorId) {
    Quest quest = questAcess.loadForWrite(questId, creatorId);
    if (quest.getVisibility() == QuestVisibility.DRAFT) {
      log.info("Quest already draft questId={} creatorId={}", questId, creatorId);
      return quest;
    }

    quest.unpublish();
    Quest saved = questRepository.save(quest);
    log.info("Quest unpublished questId={} creatorId={}", saved.getId(), creatorId);
    return saved;
  }
}