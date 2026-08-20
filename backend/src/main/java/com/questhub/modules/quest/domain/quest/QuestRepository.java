package com.questhub.modules.quest.domain.quest;

import java.util.Optional;
import java.util.UUID;

public interface QuestRepository {

  Quest save(Quest quest);

  Optional<Quest> findById(UUID id);

  boolean existsById(UUID id);

  boolean existsByCreatorIdAndVisibility(UUID creatorId, QuestVisibility visibility);

  Optional<Quest> findQuestByTaskId(UUID taskId);

  Optional<Quest> findQuestByResourceId(UUID resourceId);

  void incrementForkCount(UUID questId);
}