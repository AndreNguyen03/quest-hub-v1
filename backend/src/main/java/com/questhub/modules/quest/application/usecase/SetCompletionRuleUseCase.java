package com.questhub.modules.quest.application.usecase;

import com.questhub.modules.quest.application.helper.QuestAcess;
import com.questhub.modules.quest.domain.quest.CompletionRule;
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
public class SetCompletionRuleUseCase {

  private final QuestAcess questAcess;
  private final QuestRepository questRepository;

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public Quest setRule(UUID questId, UUID creatorId, CompletionRule rule) {
    Quest quest = questAcess.loadForWrite(questId, creatorId);
    if (!quest.isDraft()) {
      log.warn("Cannot set completion rule on non-draft quest questId={}", questId);
      throw BusinessException.conflict(
          ErrorCodes.CONFLICT, "Chỉ có thể cấu hình completion rule khi quest ở trạng thái DRAFT");
    }

    quest.applyCompletionRule(rule);
    Quest saved = questRepository.save(quest);
    log.info("Completion rule set questId={} type={} creatorId={}",
        saved.getId(), saved.getCompletionRule().type(), creatorId);
    return saved;
  }
}

