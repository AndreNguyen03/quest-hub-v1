package com.questhub.modules.marketplace.infrastructure.elasticsearch;

import java.util.UUID;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticsearchQuestRepository extends ElasticsearchRepository<QuestDocument, UUID> {

  void deleteById(UUID id);
}
