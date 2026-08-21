package com.questhub.modules.quest.application.usecase;

import com.questhub.modules.quest.application.event.QuestEventPublisher;
import com.questhub.modules.quest.application.event.TaskEventPublisher;
import com.questhub.modules.quest.domain.personalquest.PersonalQuest;
import com.questhub.modules.quest.domain.personalquest.PersonalQuestRepository;
import com.questhub.modules.quest.domain.personalquest.PersonalTask;
import com.questhub.modules.quest.domain.personalquest.TaskCompletionRepository;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class UndoTaskUseCase {

  private final PersonalQuestRepository personalQuestRepository;
  private final TaskCompletionRepository taskCompletionRepository;
  private final EvaluateCompletionUseCase evaluateCompletion;
  private final TaskEventPublisher taskEventPublisher;
  private final QuestEventPublisher questEventPublisher;

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public PersonalQuest undo(UUID personalQuestId, UUID personalTaskId, UUID userId) {
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
    if (!task.isCompleted()) {
      log.info(
          "Undo is no-op personalTaskId={} already incomplete personalQuestId={} userId={}",
          personalTaskId, personalQuestId, userId);
      return personalQuest;
    }

    boolean wasCompleted = personalQuest.isCompleted();

    personalQuest.undoTask(personalTaskId);
    evaluateCompletion.evaluate(personalQuest, userId);
    personalQuestRepository.save(personalQuest);
    taskCompletionRepository.deleteByPersonalTaskId(personalTaskId);

    taskEventPublisher.publishUndone(personalQuest, task, userId);
    if (wasCompleted && personalQuest.isActive()) {
      log.info(
          "Quest reopened after undo personalQuestId={} userId={}",
          personalQuestId, userId);
      questEventPublisher.publishReopened(personalQuest, userId);
    }
    return personalQuest;
  }
}