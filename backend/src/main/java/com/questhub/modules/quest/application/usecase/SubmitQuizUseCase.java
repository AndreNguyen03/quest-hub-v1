package com.questhub.modules.quest.application.usecase;

import com.questhub.modules.quest.application.event.QuestEventPublisher;
import com.questhub.modules.quest.application.event.TaskEventPublisher;
import com.questhub.modules.quest.domain.personalquest.PersonalQuest;
import com.questhub.modules.quest.domain.personalquest.PersonalQuestRepository;
import com.questhub.modules.quest.domain.personalquest.PersonalTask;
import com.questhub.modules.quest.domain.personalquest.QuizAttempt;
import com.questhub.modules.quest.domain.personalquest.QuizAttemptRepository;
import com.questhub.modules.quest.domain.personalquest.QuizGrader;
import com.questhub.modules.quest.domain.personalquest.TaskCompletion;
import com.questhub.modules.quest.domain.personalquest.TaskCompletionRepository;
import com.questhub.modules.quest.domain.task.TaskType;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.DomainValidationException;
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
public class SubmitQuizUseCase {

  private final PersonalQuestRepository personalQuestRepository;
  private final QuizAttemptRepository quizAttemptRepository;
  private final TaskCompletionRepository taskCompletionRepository;
  // Pure domain service — stateless, không cần Spring DI.
  private final QuizGrader quizGrader = new QuizGrader();
  private final EvaluateCompletionUseCase evaluateCompletion;
  private final TaskEventPublisher taskEventPublisher;
  private final QuestEventPublisher questEventPublisher;

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public Result submit(
      UUID personalQuestId, UUID personalTaskId, UUID userId, Map<String, Object> answers) {
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
    if (task.getType() != TaskType.QUIZ) {
      throw new DomainValidationException("Task không phải loại QUIZ");
    }

    QuizGrader.QuizScore grade = quizGrader.grade(task.getConfig(), answers);
    QuizAttempt attempt =
        quizAttemptRepository.save(
            QuizAttempt.create(
                personalTaskId, userId, grade.score(), grade.maxScore(), grade.passed(), answers));
    log.info(
        "Quiz submitted personalTaskId={} personalQuestId={} userId={} score={}/{} passed={}",
        personalTaskId, personalQuestId, userId, grade.score(), grade.maxScore(), grade.passed());

    boolean taskCompleted = false;
    if (grade.passed() && !task.isCompleted()) {
      personalQuest.completeTaskByQuiz(personalTaskId);
      boolean questCompleted = evaluateCompletion.evaluate(personalQuest, userId);
      personalQuestRepository.save(personalQuest);
      taskCompletionRepository.save(
          TaskCompletion.create(
              personalTaskId,
              userId,
              Map.of(
                  "score", grade.score(),
                  "maxScore", grade.maxScore(),
                  "passed", grade.passed())));
      taskEventPublisher.publishCompleted(personalQuest, task, userId);
      if (questCompleted) {
        log.info(
            "Quest completed personalQuestId={} questId={} userId={}",
            personalQuestId, personalQuest.getQuestId(), userId);
        questEventPublisher.publishCompleted(personalQuest, userId);
      }
      taskCompleted = true;
    }

    return new Result(attempt, taskCompleted);
  }

  public record Result(QuizAttempt attempt, boolean taskCompleted) {}
}





