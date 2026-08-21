package com.questhub.modules.quest.application.query;

import com.questhub.modules.quest.application.dto.PersonalQuestSummaryDto;
import com.questhub.modules.quest.domain.personalquest.PersonalQuestRepository;
import com.questhub.modules.quest.domain.personalquest.PersonalQuestStatus;
import com.questhub.shared.annotation.UseCase;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class ListPersonalQuestsByDomainQuery {

  private final PersonalQuestRepository personalQuestRepository;

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public List<PersonalQuestSummaryDto> byUserAndDomain(UUID userId, UUID domainId) {
    return personalQuestRepository
        .findByUserIdAndDomainIdAndStatusIn(
            userId, domainId, List.of(PersonalQuestStatus.ACTIVE, PersonalQuestStatus.COMPLETED))
        .stream()
        .map(
            pq ->
                new PersonalQuestSummaryDto(
                    pq.getId(), pq.getQuestId(), pq.getTitle(), pq.getStatus().name(), pq.getProgress()))
        .toList();
  }
}
