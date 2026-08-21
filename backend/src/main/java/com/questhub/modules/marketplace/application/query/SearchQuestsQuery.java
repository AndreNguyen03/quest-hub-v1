package com.questhub.modules.marketplace.application.query;

import com.questhub.modules.marketplace.infrastructure.elasticsearch.QuestDocument;
import com.questhub.modules.marketplace.infrastructure.elasticsearch.QuestIndexer;
import com.questhub.shared.annotation.UseCase;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class SearchQuestsQuery {

  private final QuestIndexer questIndexer;

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public List<QuestDocument> search(String keyword, UUID domainId, int page, int limit) {
    return questIndexer.search(keyword, domainId, page, limit);
  }
}
