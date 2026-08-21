package com.questhub.modules.quest.domain.personalquest;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersonalQuestRepository {

  PersonalQuest save(PersonalQuest personalQuest);

  Optional<PersonalQuest> findByIdAndUserId(UUID id, UUID userId);

  boolean existsByUserIdAndQuestId(UUID userId, UUID questId);

  List<PersonalQuest> findByUserId(UUID userId);

  List<PersonalQuest> findByUserIdAndDomainIdAndStatusIn(
      UUID userId, UUID domainId, Collection<PersonalQuestStatus> statuses);

  long countByUserIdAndStatus(UUID userId, PersonalQuestStatus status);

  List<LeaderboardStat> topByCompletionStats(int limit);
}