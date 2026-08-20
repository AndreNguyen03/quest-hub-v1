package com.questhub.modules.quest.application.usecase;

import com.questhub.modules.quest.domain.quest.Quest;
import com.questhub.modules.quest.domain.quest.QuestRepository;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class GetQuestUseCase {

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
    if (quest.getVisibility() != com.questhub.modules.quest.domain.quest.QuestVisibility.PUBLIC
        && !quest.getCreatorId().equals(viewerId)) {
      throw BusinessException.forbidden(ErrorCodes.FORBIDDEN, "Không có quyền xem quest này");
    }
    return quest;
  }
}