package com.questhub.modules.quest.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.questhub.modules.quest.application.event.TaskEventPublisher;
import com.questhub.modules.quest.domain.personalquest.PersonalChapter;
import com.questhub.modules.quest.domain.personalquest.PersonalQuest;
import com.questhub.modules.quest.domain.personalquest.PersonalQuestRepository;
import com.questhub.modules.quest.domain.personalquest.PersonalTask;
import com.questhub.modules.quest.domain.personalquest.TaskCompletionRepository;
import com.questhub.modules.quest.domain.quest.CompletionRule;
import com.questhub.modules.quest.domain.quest.TaskType;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UndoTaskUseCaseTest {

  @Mock private PersonalQuestRepository personalQuestRepository;
  @Mock private TaskCompletionRepository taskCompletionRepository;
  @Mock private EvaluateCompletionUseCase evaluateCompletion;
  @Mock private TaskEventPublisher taskEventPublisher;

  @InjectMocks private UndoTaskUseCase useCase;

  @Test
  void undo_completedTask_shouldResetAndDeleteCompletionAndPublishEvent() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = personalQuest(userId);
    UUID taskId = quest.getChapters().get(0).getTasks().get(0).getId();
    quest.completeTask(taskId, Map.of());
    assertThat(quest.getProgress()).isEqualTo(50);
    when(personalQuestRepository.findByIdAndUserId(quest.getId(), userId))
        .thenReturn(Optional.of(quest));
    when(personalQuestRepository.save(any(PersonalQuest.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    PersonalQuest result = useCase.undo(quest.getId(), taskId, userId);

    assertThat(result.getChapters().get(0).getTasks().get(0).isCompleted()).isFalse();
    assertThat(result.getChapters().get(0).getTasks().get(0).getCompletedAt()).isNull();
    assertThat(result.getProgress()).isEqualTo(0);
    verify(evaluateCompletion).evaluate(eq(quest), eq(userId));
    verify(taskCompletionRepository).deleteByPersonalTaskId(taskId);
    verify(taskEventPublisher).publishUndone(eq(quest), eq(quest.getChapters().get(0).getTasks().get(0)), eq(userId));
  }

  @Test
  void undo_incompleteTask_shouldBeNoOp() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = personalQuest(userId);
    UUID taskId = quest.getChapters().get(0).getTasks().get(0).getId();
    when(personalQuestRepository.findByIdAndUserId(quest.getId(), userId))
        .thenReturn(Optional.of(quest));

    PersonalQuest result = useCase.undo(quest.getId(), taskId, userId);

    assertThat(result.getProgress()).isEqualTo(0);
    verify(personalQuestRepository, never()).save(any());
    verify(taskCompletionRepository, never()).deleteByPersonalTaskId(any());
    verify(taskEventPublisher, never()).publishUndone(any(), any(), any());
  }

  @Test
  void undo_unknownTask_shouldThrowNotFound() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = personalQuest(userId);
    when(personalQuestRepository.findByIdAndUserId(quest.getId(), userId))
        .thenReturn(Optional.of(quest));

    BusinessException ex =
        catchThrowableOfType(
            () -> useCase.undo(quest.getId(), UUID.randomUUID(), userId),
            BusinessException.class);

    assertThat(ex.getCode()).isEqualTo(ErrorCodes.NOT_FOUND);
    verify(taskCompletionRepository, never()).deleteByPersonalTaskId(any());
  }

  @Test
  void undo_unknownQuest_shouldThrowNotFound() {
    UUID questId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    when(personalQuestRepository.findByIdAndUserId(questId, userId))
        .thenReturn(Optional.empty());

    BusinessException ex =
        catchThrowableOfType(
            () -> useCase.undo(questId, UUID.randomUUID(), userId), BusinessException.class);

    assertThat(ex.getCode()).isEqualTo(ErrorCodes.NOT_FOUND);
    verify(taskCompletionRepository, never()).deleteByPersonalTaskId(any());
  }

  private PersonalQuest personalQuest(UUID userId) {
    PersonalQuest quest =
        PersonalQuest.create(
            userId,
            UUID.randomUUID(),
            null,
            "Spring Security Fundamentals",
            CompletionRule.quizScore(new BigDecimal("70")));
    PersonalChapter chapter = PersonalChapter.create(null, "Authentication", "Phần 1", 0);
    chapter.addTask(PersonalTask.create(null, TaskType.LEARN, "Xem video", "v", 0, Map.of()));
    chapter.addTask(PersonalTask.create(null, TaskType.PRACTICE, "Code", "c", 1, Map.of()));
    quest.addChapter(chapter);
    return quest;
  }
}