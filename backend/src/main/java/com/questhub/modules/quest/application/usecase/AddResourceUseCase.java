package com.questhub.modules.quest.application.usecase;

import com.questhub.modules.quest.application.helper.QuestAcess;
import com.questhub.modules.quest.application.request.AddResourceRequest;
import com.questhub.modules.quest.domain.quest.Quest;
import com.questhub.modules.quest.domain.quest.QuestRepository;
import com.questhub.modules.quest.domain.quest.Resource;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class AddResourceUseCase {
  private final QuestAcess questAcess;
  private final QuestRepository questRepository;

  @Transactional(
      isolation = Isolation.DEFAULT,
      propagation = Propagation.REQUIRED,
      rollbackFor = Exception.class)
  public Quest add(UUID taskId, UUID actorId, AddResourceRequest request) {
    Quest quest =
        questRepository
            .findQuestByTaskId(taskId)
            .orElseThrow(
                () -> BusinessException.notFound(ErrorCodes.NOT_FOUND, "Không tìm thấy task"));
    questAcess.verifyCreator(quest, actorId);
    quest.addResource(
        taskId,
        Resource.create(request.type(), request.title(), request.url(), request.estimatedMinutes()));
    Quest saved = questRepository.save(quest);
    log.info(
        "Resource added taskId={} actorId={} type={} url={}",
        taskId, actorId, request.type(), request.url());
    return saved;
  }
}