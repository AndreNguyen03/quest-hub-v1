package com.questhub.modules.quest.infrastructure.persistence;

import com.questhub.modules.quest.domain.quest.Quest;
import com.questhub.modules.quest.domain.quest.QuestRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class QuestRepositoryImpl implements QuestRepository {

  private final SpringDataQuestRepository jpa;

  public QuestRepositoryImpl(SpringDataQuestRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Quest save(Quest quest) {
    return QuestMapper.toDomain(jpa.save(QuestMapper.toEntity(quest)));
  }

  @Override
  public Optional<Quest> findById(UUID id) {
    return jpa.findById(id).map(QuestMapper::toDomain);
  }

  @Override
  public boolean existsById(UUID id) {
    return jpa.existsById(id);
  }
}