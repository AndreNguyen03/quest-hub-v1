package com.questhub.modules.admin.application.usecase;

import com.questhub.modules.admin.infrastructure.persistence.quest.SpringDataAdminQuestRepository;
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
public class HideQuestUseCase {

  private final SpringDataAdminQuestRepository questRepository;

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public void hide(UUID questId) {
    int updated = questRepository.updateVisibility(questId, "HIDDEN");
    if (updated == 0) {
      throw BusinessException.notFound(ErrorCodes.NOT_FOUND, "Không tìm thấy quest");
    }
  }
}
