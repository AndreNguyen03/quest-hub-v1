package com.questhub.modules.marketplace.application.usecase;

import com.questhub.modules.marketplace.application.query.PopularQuestsQuery;
import com.questhub.modules.quest.application.dto.QuestDto;
import com.questhub.shared.annotation.UseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class GetPopularQuestsUseCase {

  private final PopularQuestsQuery getPopularQuestsQuery;

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public List<QuestDto> get(int limit) {
    return getPopularQuestsQuery.get(limit);
  }
}
