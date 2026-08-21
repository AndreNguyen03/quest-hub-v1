package com.questhub.modules.quest.application.query;

import com.questhub.modules.quest.domain.personalquest.PersonalQuest;
import com.questhub.modules.quest.domain.personalquest.PersonalQuestRepository;
import com.questhub.shared.annotation.UseCase;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class GetPersonalQuestsQuery {

  private final PersonalQuestRepository personalQuestRepository;

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public List<PersonalQuest> list(UUID userId) {
    log.info("Personal quests listed userId={}", userId);
    return personalQuestRepository.findByUserId(userId);
  }
}
