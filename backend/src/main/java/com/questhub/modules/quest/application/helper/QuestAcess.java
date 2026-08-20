package com.questhub.modules.quest.application.helper;

import com.questhub.modules.quest.domain.quest.Quest;
import com.questhub.modules.quest.domain.quest.QuestRepository;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestAcess {
  private final QuestRepository questRepository;

  public Quest loadForWrite(UUID questId, UUID actorId) {
    Quest quest =
        questRepository
            .findById(questId)
            .orElseThrow(
                () -> BusinessException.notFound(ErrorCodes.NOT_FOUND, "Không tìm thấy quest"));
    return verifyCreator(quest, actorId);
  }

  public Quest verifyCreator(Quest quest, UUID actorId) {
    if (!quest.getCreatorId().equals(actorId)) {
      log.warn(
          "Forbidden access questId={} actorId={} ownerId={}",
          quest.getId(), actorId, quest.getCreatorId());
      throw BusinessException.forbidden(ErrorCodes.FORBIDDEN, "Chỉ creator mới sửa được quest");
    }

    return quest;
  }
}
