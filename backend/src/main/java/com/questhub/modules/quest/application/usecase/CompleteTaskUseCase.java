package com.questhub.modules.quest.application.usecase;

import com.questhub.modules.quest.application.event.QuestEventPublisher;
import com.questhub.modules.quest.application.event.TaskEventPublisher;
import com.questhub.modules.quest.domain.personalquest.PersonalQuest;
import com.questhub.modules.quest.domain.personalquest.PersonalQuestRepository;
import com.questhub.modules.quest.domain.personalquest.PersonalTask;
import com.questhub.modules.quest.domain.personalquest.TaskCompletion;
import com.questhub.modules.quest.domain.personalquest.TaskCompletionRepository;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class CompleteTaskUseCase {

  private final PersonalQuestRepository personalQuestRepository;
  private final TaskCompletionRepository taskCompletionRepository;
  private final EvaluateCompletionUseCase evaluateCompletion;
  private final TaskEventPublisher taskEventPublisher;
  private final QuestEventPublisher questEventPublisher;

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public PersonalQuest complete(
      UUID personalQuestId, UUID personalTaskId, UUID userId, Map<String, Object> evidence) {
    PersonalQuest personalQuest =
        personalQuestRepository
            .findByIdAndUserId(personalQuestId, userId)
            .orElseThrow(
                () ->
                    BusinessException.notFound(
                        ErrorCodes.NOT_FOUND, "Không tìm thấy personal quest"));
    PersonalTask task =
        personalQuest
            .findTask(personalTaskId)
            .orElseThrow(
                () -> BusinessException.notFound(ErrorCodes.NOT_FOUND, "Không tìm thấy task"));
    if (task.isCompleted()) {
      log.warn(
          "Task already completed personalTaskId={} personalQuestId={} userId={}",
          personalTaskId, personalQuestId, userId);
      throw BusinessException.conflict(
          ErrorCodes.CONFLICT, "Task đã hoàn thành trước đó");
    }

    personalQuest.completeTask(personalTaskId, evidence);
    boolean questCompleted = evaluateCompletion.evaluate(personalQuest, userId);
    personalQuestRepository.save(personalQuest);
    taskCompletionRepository.save(TaskCompletion.create(personalTaskId, userId, evidence));

    taskEventPublisher.publishCompleted(personalQuest, task, userId);
    if (questCompleted) {
      log.info(
          "Quest completed personalQuestId={} questId={} userId={}",
          personalQuestId, personalQuest.getQuestId(), userId);
      questEventPublisher.publishCompleted(personalQuest, userId);
    }
    return personalQuest;
  }
}


