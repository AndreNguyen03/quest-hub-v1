package com.questhub.modules.marketplace.application.usecase;

import com.questhub.modules.marketplace.application.query.TrendingQuestsQuery;
import com.questhub.modules.quest.application.dto.QuestDto;
import com.questhub.shared.annotation.UseCase;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class GetTrendingQuestsUseCase {

  private final TrendingQuestsQuery getTrendingQuestsQuery;

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public List<QuestDto> get(Instant since, int limit) {
    return getTrendingQuestsQuery.get(since, limit);
  }
}
