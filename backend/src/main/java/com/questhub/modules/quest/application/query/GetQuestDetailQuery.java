package com.questhub.modules.quest.application.query;

import com.questhub.modules.quest.domain.quest.Quest;
import com.questhub.shared.annotation.UseCase;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class GetQuestDetailQuery {

  private final GetQuestQuery getQuestQuery;

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public Quest get(UUID questId, UUID viewerId) {
    Quest quest = getQuestQuery.get(questId, viewerId);
    log.info("Quest detail viewed questId={} viewerId={}", questId, viewerId);
    return quest;
  }
}

