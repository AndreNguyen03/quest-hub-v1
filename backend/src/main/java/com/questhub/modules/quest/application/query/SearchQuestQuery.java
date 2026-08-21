package com.questhub.modules.quest.application.query;

import com.questhub.modules.quest.domain.quest.Quest;
import com.questhub.modules.quest.domain.quest.QuestRepository;
import com.questhub.modules.quest.domain.quest.QuestVisibility;
import com.questhub.shared.annotation.UseCase;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class SearchQuestQuery {

  private final QuestRepository questRepository;

  public record SearchResult(List<Quest> quests, long total) {}

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public SearchResult search(String keyword, UUID domainId, String difficulty, int page, int limit) {
    // TODO: replace with Elasticsearch when available, fallback to DB search via QuestRepository
    log.info("Search quests keyword={} domainId={} difficulty={} page={} limit={}", keyword, domainId, difficulty, page, limit);
    List<Quest> all = questRepository.findAll().stream()
        .filter(q -> q.getVisibility() == QuestVisibility.PUBLIC)
        .filter(q -> keyword == null || q.getTitle().toLowerCase().contains(keyword.toLowerCase()))
        .toList();
    long total = all.size();
    int from = Math.min(page * limit, all.size());
    int to = Math.min(from + limit, all.size());
    return new SearchResult(all.subList(from, to), total);
  }
}

