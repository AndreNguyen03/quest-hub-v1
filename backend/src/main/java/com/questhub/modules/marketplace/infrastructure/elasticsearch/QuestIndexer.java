package com.questhub.modules.marketplace.infrastructure.elasticsearch;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.questhub.modules.marketplace.infrastructure.elasticsearch.QuestDocument;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestIndexer {

  private final ElasticsearchQuestRepository repository;
  private final ElasticsearchOperations operations;

  public void index(QuestDocument doc) {
    repository.save(doc);
    log.info("Quest indexed questId={}", doc.getId());
  }

  public void update(QuestDocument doc) {
    index(doc);
  }

  public void delete(UUID questId) {
    repository.deleteById(questId);
    log.info("Quest deleted from index questId={}", questId);
  }

  public List<QuestDocument> search(String keyword, UUID domainId, int page, int limit) {
    NativeQuery nativeQuery =
        NativeQuery.builder()
            .withQuery(searchQuery(keyword, domainId))
            .withPageable(PageRequest.of(page, limit))
            .build();
    SearchHits<QuestDocument> hits = operations.search(nativeQuery, QuestDocument.class);
    return hits.getSearchHits().stream().map(SearchHit::getContent).toList();
  }

  private Query searchQuery(String keyword, UUID domainId) {
    List<Query> must = new ArrayList<>();
    if (keyword != null && !keyword.isBlank()) {
      must.add(
          Query.of(
              q ->
                  q.multiMatch(
                      m ->
                          m.fields("title", "description").query(keyword).fuzziness("AUTO"))));
    }
    if (domainId != null) {
      must.add(
          Query.of(
              q -> q.term(t -> t.field("domainId").value(domainId.toString()))));
    }
    return Query.of(
        q -> q.bool(b -> b.must(must)));
  }
}