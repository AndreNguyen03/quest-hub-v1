package com.questhub.modules.quest.application.usecase;

import com.questhub.modules.quest.domain.personalquest.PersonalQuest;
import com.questhub.modules.quest.domain.personalquest.PersonalQuestRepository;
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
public class AbandonQuestUseCase {

  private final PersonalQuestRepository personalQuestRepository;

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public void abandon(UUID personalQuestId, UUID userId) {
    PersonalQuest personalQuest =
        personalQuestRepository
            .findByIdAndUserId(personalQuestId, userId)
            .orElseThrow(
                () ->
                    BusinessException.notFound(
                        ErrorCodes.NOT_FOUND, "Không tìm thấy personal quest"));

    personalQuest.abandon();
    personalQuestRepository.save(personalQuest);

    log.info("Quest abandoned personalQuestId={} userId={}", personalQuestId, userId);
  }
}
