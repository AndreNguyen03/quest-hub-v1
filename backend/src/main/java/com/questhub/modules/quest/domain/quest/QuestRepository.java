package com.questhub.modules.quest.domain.quest;

import java.util.Optional;
import java.util.UUID;

public interface QuestRepository {

  Quest save(Quest quest);

  Optional<Quest> findById(UUID id);

  boolean existsById(UUID id);
}