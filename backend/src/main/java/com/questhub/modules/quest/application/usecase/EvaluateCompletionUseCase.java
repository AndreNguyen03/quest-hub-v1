package com.questhub.modules.quest.application.usecase;

import com.questhub.modules.quest.domain.personalquest.CompletionEvaluator;
import com.questhub.modules.quest.domain.personalquest.PersonalQuest;
import com.questhub.modules.quest.domain.personalquest.PersonalTask;
import com.questhub.modules.quest.domain.personalquest.QuizAttempt;
import com.questhub.modules.quest.domain.personalquest.QuizAttemptRepository;
import com.questhub.modules.quest.domain.quest.TaskType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class EvaluateCompletionUseCase {

  private static final BigDecimal HUNDRED = new BigDecimal("100");

  private final CompletionEvaluator evaluator;
  private final QuizAttemptRepository quizAttemptRepository;

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public boolean evaluate(PersonalQuest quest, UUID userId) {
    Map<UUID, BigDecimal> bestQuizPercent = bestQuizPercent(quest);
    boolean satisfied = evaluator.isSatisfied(quest.getCompletionRule(), quest, bestQuizPercent);
    if (satisfied && !quest.isCompleted()) {
      quest.markCompleted();
      return true;
    }
    if (!satisfied && quest.isCompleted()) {
      quest.reopen();
    }
    return false;
  }

  private Map<UUID, BigDecimal> bestQuizPercent(PersonalQuest quest) {
    Map<UUID, BigDecimal> best = new HashMap<>();
    for (PersonalTask task : quest.getAllTasks()) {
      if (task.getType() != TaskType.QUIZ) {
        continue;
      }
      BigDecimal bestPercent = BigDecimal.ZERO;
      List<QuizAttempt> attempts =
          quizAttemptRepository.findByPersonalTaskIdOrderByCreatedAtDesc(task.getId());
      for (QuizAttempt attempt : attempts) {
        if (attempt.getMaxScore().compareTo(BigDecimal.ZERO) <= 0) {
          continue;
        }
        BigDecimal percent =
            attempt
                .getScore()
                .multiply(HUNDRED)
                .divide(attempt.getMaxScore(), 2, RoundingMode.HALF_UP);
        if (percent.compareTo(bestPercent) > 0) {
          bestPercent = percent;
        }
      }
      best.put(task.getId(), bestPercent);
    }
    return best;
  }
}