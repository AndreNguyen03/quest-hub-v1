package com.questhub.modules.quest.application.query;

import com.questhub.modules.quest.domain.personalquest.PersonalQuest;
import com.questhub.modules.quest.domain.personalquest.PersonalQuestRepository;
import com.questhub.modules.quest.domain.personalquest.PersonalTask;
import com.questhub.modules.quest.domain.personalquest.QuizAttempt;
import com.questhub.modules.quest.domain.personalquest.QuizAttemptRepository;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class GetQuizHistoryQuery {

  private final PersonalQuestRepository personalQuestRepository;
  private final QuizAttemptRepository quizAttemptRepository;

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public List<QuizAttempt> get(UUID personalQuestId, UUID personalTaskId, UUID userId) {
    PersonalQuest personalQuest =
        personalQuestRepository
            .findByIdAndUserId(personalQuestId, userId)
            .orElseThrow(
                () ->
                    BusinessException.notFound(
                        ErrorCodes.NOT_FOUND, "Không tìm thấy personal quest"));
    personalQuest
        .findTask(personalTaskId)
        .orElseThrow(
            () -> BusinessException.notFound(ErrorCodes.NOT_FOUND, "Không tìm thấy task"));
    List<QuizAttempt> attempts =
        quizAttemptRepository.findByPersonalTaskIdOrderByCreatedAtDesc(personalTaskId);
    log.info(
        "Quiz history viewed personalTaskId={} personalQuestId={} userId={} count={}",
        personalTaskId, personalQuestId, userId, attempts.size());
    return attempts;
  }
}
