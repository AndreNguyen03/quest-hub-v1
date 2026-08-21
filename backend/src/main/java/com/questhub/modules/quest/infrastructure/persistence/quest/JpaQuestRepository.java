package com.questhub.modules.quest.infrastructure.persistence.quest;

import com.questhub.modules.quest.domain.quest.Quest;
import com.questhub.modules.quest.domain.quest.QuestRepository;
import com.questhub.modules.quest.domain.quest.QuestVisibility;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class JpaQuestRepository implements QuestRepository {

  private final SpringDataQuestRepository jpa;

  public JpaQuestRepository(SpringDataQuestRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Quest save(Quest quest) {
    return QuestMapper.toDomain(jpa.save(QuestMapper.toEntity(quest)));
  }

  @Override
  public List<Quest> findAll() {
    return jpa.findAll().stream().map(QuestMapper::toDomain).toList();
  }

  @Override
  public Optional<Quest> findById(UUID id) {
    return jpa.findById(id).map(QuestMapper::toDomain);
  }

  @Override
  public boolean existsById(UUID id) {
    return jpa.existsById(id);
  }

  @Override
  public boolean existsByCreatorIdAndVisibility(UUID creatorId, QuestVisibility visibility) {
    return jpa.existsByCreatorIdAndVisibility(creatorId, visibility);
  }

  @Override
  public Optional<Quest> findQuestByTaskId(UUID taskId) {
    return jpa.findByTaskId(taskId).map(QuestMapper::toDomain);
  }

  @Override
  public Optional<Quest> findQuestByResourceId(UUID resourceId) {
    return jpa.findByResourceId(resourceId).map(QuestMapper::toDomain);
  }

  @Override
  public void incrementForkCount(UUID questId) {
    jpa.incrementForkCount(questId);
  }

  @Override
  public List<Quest> findPopular(int limit) {
    return jpa.findPopular(QuestVisibility.PUBLIC, PageRequest.of(0, limit)).stream()
        .map(QuestMapper::toDomain)
        .toList();
  }

  @Override
  public List<Quest> findTrending(Instant since, int limit) {
    return jpa.findTrending(QuestVisibility.PUBLIC, since, PageRequest.of(0, limit)).stream()
        .map(QuestMapper::toDomain)
        .toList();
  }
}



