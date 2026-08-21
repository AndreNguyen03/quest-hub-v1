package com.questhub.modules.quest.application.query;

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
public class GetQuestVisibilityQuery {

  private final QuestRepository questRepository;

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public String get(UUID questId) {
    return questRepository
        .findById(questId)
        .map(q -> q.getVisibility().name())
        .orElseThrow(
            () -> BusinessException.notFound(ErrorCodes.NOT_FOUND, "Không tìm thấy quest"));
  }
}
