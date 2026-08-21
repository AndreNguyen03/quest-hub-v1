package com.questhub.modules.quest.infrastructure.persistence.personalquest;

import com.questhub.modules.quest.domain.personalquest.LeaderboardStat;
import com.questhub.modules.quest.domain.personalquest.PersonalQuest;
import com.questhub.modules.quest.domain.personalquest.PersonalQuestRepository;
import com.questhub.modules.quest.domain.personalquest.PersonalQuestStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaPersonalQuestRepository implements PersonalQuestRepository {

  private final SpringDataPersonalQuestRepository jpa;

  public JpaPersonalQuestRepository(SpringDataPersonalQuestRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public PersonalQuest save(PersonalQuest quest) {
    return PersonalQuestMapper.toDomain(jpa.save(PersonalQuestMapper.toEntity(quest)));
  }

  @Override
  public Optional<PersonalQuest> findByIdAndUserId(UUID id, UUID userId) {
    return jpa.findByIdAndUserId(id, userId).map(PersonalQuestMapper::toDomain);
  }

  @Override
  public boolean existsByUserIdAndQuestId(UUID userId, UUID questId) {
    return jpa.existsByUserIdAndQuestId(userId, questId);
  }

  @Override
  public List<PersonalQuest> findByUserId(UUID userId) {
    return jpa.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(PersonalQuestMapper::toDomain)
        .toList();
  }

  @Override
  public List<PersonalQuest> findByUserIdAndDomainIdAndStatusIn(
      UUID userId, UUID domainId, Collection<PersonalQuestStatus> statuses) {
    return jpa.findByUserIdAndDomainIdAndStatusIn(userId, domainId, statuses).stream()
        .map(PersonalQuestMapper::toDomain)
        .toList();
  }

  @Override
  public long countByUserIdAndStatus(UUID userId, PersonalQuestStatus status) {
    return jpa.countByUserIdAndStatus(userId, status);
  }

  @Override
  public List<LeaderboardStat> topByCompletionStats(int limit) {
    return jpa.findTopCompletionStats(limit).stream()
        .map(row -> new LeaderboardStat(row.getUserId(), row.getQuestCount(), row.getTaskCount()))
        .toList();
  }
}




