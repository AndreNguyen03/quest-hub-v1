package com.questhub.modules.quest.application.query;

import com.questhub.modules.quest.domain.personalquest.PersonalQuestRepository;
import com.questhub.shared.annotation.UseCase;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class ExistsPersonalQuestQuery {

  private final PersonalQuestRepository personalQuestRepository;

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public boolean exists(UUID userId, UUID questId) {
    return personalQuestRepository.existsByUserIdAndQuestId(userId, questId);
  }
}
