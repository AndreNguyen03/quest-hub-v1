package com.questhub.modules.quest.application.query;

import com.questhub.modules.quest.domain.quest.Quest;
import com.questhub.modules.quest.domain.quest.QuestRepository;
import com.questhub.modules.quest.domain.quest.QuestVisibility;
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
public class GetQuestQuery {

  private final QuestRepository questRepository;

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public Quest get(UUID questId, UUID viewerId) {
    Quest quest =
        questRepository
            .findById(questId)
            .orElseThrow(
                () -> BusinessException.notFound(ErrorCodes.NOT_FOUND, "Không tìm thấy quest"));
    if (quest.getVisibility() != QuestVisibility.PUBLIC && !quest.getCreatorId().equals(viewerId)) {
      log.warn("Forbidden view quest questId={} viewerId={} ownerId={}",
          questId, viewerId, quest.getCreatorId());
      throw BusinessException.forbidden(ErrorCodes.FORBIDDEN, "Không có quyền xem quest này");
    }
    log.info("Quest viewed questId={} viewerId={}", questId, viewerId);
    return quest;
  }
}


