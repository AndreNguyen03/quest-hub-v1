package com.questhub.modules.admin.application.query;

import com.questhub.modules.admin.infrastructure.persistence.quest.AdminQuestJpaEntity;
import com.questhub.modules.admin.infrastructure.persistence.quest.SpringDataAdminQuestRepository;
import com.questhub.shared.annotation.UseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class ListAllQuestsQuery {

  private final SpringDataAdminQuestRepository questRepository;

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public List<AdminQuestJpaEntity> list(int page, int limit) {
    return questRepository.findAllQuests(PageRequest.of(page, limit));
  }
}
