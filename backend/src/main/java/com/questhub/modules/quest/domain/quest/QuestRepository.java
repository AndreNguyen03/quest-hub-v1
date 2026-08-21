package com.questhub.modules.quest.domain.quest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestRepository {

  Quest save(Quest quest);

  List<Quest> findAll();

  List<Quest> findPopular(int limit);

  List<Quest> findTrending(Instant since, int limit);

  Optional<Quest> findById(UUID id);

  boolean existsById(UUID id);

  boolean existsByCreatorIdAndVisibility(UUID creatorId, QuestVisibility visibility);

  Optional<Quest> findQuestByTaskId(UUID taskId);

  Optional<Quest> findQuestByResourceId(UUID resourceId);

  void incrementForkCount(UUID questId);
}

