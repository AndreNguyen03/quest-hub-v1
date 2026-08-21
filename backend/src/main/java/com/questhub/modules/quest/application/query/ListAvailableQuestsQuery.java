package com.questhub.modules.quest.application.query;

import com.questhub.modules.quest.domain.quest.Quest;
import com.questhub.modules.quest.domain.quest.QuestRepository;
import com.questhub.modules.quest.domain.quest.QuestVisibility;
import com.questhub.shared.annotation.UseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class ListAvailableQuestsQuery {

  private final QuestRepository questRepository;

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public List<Quest> list(int page, int limit) {
    log.info("List available quests page={} limit={}", page, limit);
    List<Quest> all =
        questRepository.findAll().stream()
            .filter(q -> q.getVisibility() == QuestVisibility.PUBLIC)
            .toList();
    int from = Math.min(page * limit, all.size());
    int to = Math.min(from + limit, all.size());
    return all.subList(from, to);
  }
}

