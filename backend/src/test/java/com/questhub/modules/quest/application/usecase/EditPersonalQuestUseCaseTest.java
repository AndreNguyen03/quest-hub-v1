package com.questhub.modules.quest.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.questhub.modules.quest.domain.personalquest.PersonalChapter;
import com.questhub.modules.quest.domain.personalquest.PersonalQuest;
import com.questhub.modules.quest.domain.personalquest.PersonalQuestRepository;
import com.questhub.modules.quest.domain.personalquest.PersonalTask;
import com.questhub.modules.quest.domain.quest.CompletionRule;
import com.questhub.modules.quest.domain.task.TaskType;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.DomainValidationException;
import com.questhub.shared.domain.ErrorCodes;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EditPersonalQuestUseCaseTest {

  @Mock private PersonalQuestRepository personalQuestRepository;

  @InjectMocks private EditPersonalQuestUseCase useCase;

  @Test
  void addChapter_shouldAppendChapterAndPersist() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = personalQuest(userId);
    when(personalQuestRepository.findByIdAndUserId(quest.getId(), userId))
        .thenReturn(Optional.of(quest));
    when(personalQuestRepository.save(any(PersonalQuest.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    PersonalQuest result = useCase.addChapter(quest.getId(), userId, "Custom", "desc");

    assertThat(result.getChapters()).hasSize(3);
    assertThat(result.getChapters().get(2).getTitle()).isEqualTo("Custom");
    assertThat(result.getChapters().get(2).getPosition()).isEqualTo(2);
    assertThat(result.getChapters().get(2).getSourceChapterId()).isNull();
    verify(personalQuestRepository).save(result);
  }

  @Test
  void addTask_shouldAppendTaskNotCompletedAndRecalcProgress() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = personalQuest(userId);
    UUID chapterId = quest.getChapters().get(0).getId();
    quest.completeTask(quest.getChapters().get(0).getTasks().get(0).getId(), Map.of());
    assertThat(quest.getProgress()).isEqualTo(50);
    when(personalQuestRepository.findByIdAndUserId(quest.getId(), userId))
        .thenReturn(Optional.of(quest));
    when(personalQuestRepository.save(any(PersonalQuest.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    PersonalQuest result =
        useCase.addTask(quest.getId(), chapterId, userId, TaskType.PRACTICE, "Extra", null, Map.of(), null);

    PersonalTask added = result.getChapters().get(0).getTasks().get(2);
    assertThat(added.getType()).isEqualTo(TaskType.PRACTICE);
    assertThat(added.isCompleted()).isFalse();
    assertThat(added.getOrder()).isEqualTo(2);
    assertThat(added.getSourceTaskId()).isNull();
    assertThat(result.getProgress()).isEqualTo(33);
  }

  @Test
  void addTask_unknownChapter_shouldThrow() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = personalQuest(userId);
    when(personalQuestRepository.findByIdAndUserId(quest.getId(), userId))
        .thenReturn(Optional.of(quest));

    DomainValidationException ex =
        catchThrowableOfType(
            () ->
                useCase.addTask(
                    quest.getId(), UUID.randomUUID(), userId, TaskType.PRACTICE, "x", null, Map.of(), null),
            DomainValidationException.class);

    assertThat(ex.getMessage()).contains("chapter");
    verify(personalQuestRepository, never()).save(any());
  }

  @Test
  void removeTask_shouldRemoveAndRecalcProgress() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = personalQuest(userId);
    UUID taskId = quest.getChapters().get(0).getTasks().get(0).getId();
    quest.completeTask(taskId, Map.of());
    assertThat(quest.getProgress()).isEqualTo(50);
    when(personalQuestRepository.findByIdAndUserId(quest.getId(), userId))
        .thenReturn(Optional.of(quest));
    when(personalQuestRepository.save(any(PersonalQuest.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    PersonalQuest result =
        useCase.removeTask(quest.getId(), quest.getChapters().get(0).getId(), taskId, userId);

    assertThat(result.getChapters().get(0).getTasks()).hasSize(1);
    assertThat(result.getProgress()).isEqualTo(0);
  }

  @Test
  void removeChapter_shouldRemoveAndRecalcProgress() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = personalQuest(userId);
    UUID chapterId = quest.getChapters().get(0).getId();
    when(personalQuestRepository.findByIdAndUserId(quest.getId(), userId))
        .thenReturn(Optional.of(quest));
    when(personalQuestRepository.save(any(PersonalQuest.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    PersonalQuest result = useCase.removeChapter(quest.getId(), chapterId, userId);

    assertThat(result.getChapters()).hasSize(1);
    assertThat(result.getChapters().get(0).getId())
        .isNotEqualTo(chapterId);
  }

  @Test
  void reorderChapters_shouldUpdatePositions() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = personalQuest(userId);
    UUID first = quest.getChapters().get(0).getId();
    UUID second = quest.getChapters().get(1).getId();
    when(personalQuestRepository.findByIdAndUserId(quest.getId(), userId))
        .thenReturn(Optional.of(quest));
    when(personalQuestRepository.save(any(PersonalQuest.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    PersonalQuest result =
        useCase.reorderChapters(quest.getId(), userId, List.of(second, first));

    assertThat(result.getChapters().get(0).getId()).isEqualTo(second);
    assertThat(result.getChapters().get(0).getPosition()).isEqualTo(0);
    assertThat(result.getChapters().get(1).getId()).isEqualTo(first);
    assertThat(result.getChapters().get(1).getPosition()).isEqualTo(1);
  }

  @Test
  void reorderTasks_shouldUpdateOrders() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = personalQuest(userId);
    UUID chapterId = quest.getChapters().get(0).getId();
    UUID t1 = quest.getChapters().get(0).getTasks().get(0).getId();
    UUID t2 = quest.getChapters().get(0).getTasks().get(1).getId();
    when(personalQuestRepository.findByIdAndUserId(quest.getId(), userId))
        .thenReturn(Optional.of(quest));
    when(personalQuestRepository.save(any(PersonalQuest.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    PersonalQuest result =
        useCase.reorderTasks(quest.getId(), chapterId, userId, List.of(t2, t1));

    assertThat(result.getChapters().get(0).getTasks().get(0).getId()).isEqualTo(t2);
    assertThat(result.getChapters().get(0).getTasks().get(0).getOrder()).isEqualTo(0);
    assertThat(result.getChapters().get(0).getTasks().get(1).getId()).isEqualTo(t1);
    assertThat(result.getChapters().get(0).getTasks().get(1).getOrder()).isEqualTo(1);
  }

  @Test
  void reorderChapters_mismatchedIds_shouldThrow() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = personalQuest(userId);
    when(personalQuestRepository.findByIdAndUserId(quest.getId(), userId))
        .thenReturn(Optional.of(quest));

    DomainValidationException ex =
        catchThrowableOfType(
            () ->
                useCase.reorderChapters(
                    quest.getId(), userId, List.of(UUID.randomUUID(), UUID.randomUUID())),
            DomainValidationException.class);

    assertThat(ex.getMessage()).contains("khớp");
    verify(personalQuestRepository, never()).save(any());
  }

  @Test
  void edit_otherUsersQuest_shouldThrowNotFound() {
    UUID userId = UUID.randomUUID();
    when(personalQuestRepository.findByIdAndUserId(any(), any()))
        .thenReturn(Optional.empty());

    BusinessException ex =
        catchThrowableOfType(
            () -> useCase.addChapter(UUID.randomUUID(), userId, "x", null),
            BusinessException.class);

    assertThat(ex.getCode()).isEqualTo(ErrorCodes.NOT_FOUND);
    verify(personalQuestRepository, never()).save(any());
  }

  @Test
  void edit_completedQuest_shouldThrowConflict() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = personalQuest(userId);
    quest.markCompleted();
    when(personalQuestRepository.findByIdAndUserId(quest.getId(), userId))
        .thenReturn(Optional.of(quest));

    BusinessException ex =
        catchThrowableOfType(
            () -> useCase.addChapter(quest.getId(), userId, "x", null),
            BusinessException.class);

    assertThat(ex.getCode()).isEqualTo(ErrorCodes.CONFLICT);
    verify(personalQuestRepository, never()).save(any());
  }

  private PersonalQuest personalQuest(UUID userId) {
    PersonalQuest quest =
        PersonalQuest.create(
            userId,
            UUID.randomUUID(),
            null,
            "Quest",
            CompletionRule.defaultAllTasks());
    PersonalChapter c1 = PersonalChapter.create(null, "c1", null, 0);
    c1.addTask(PersonalTask.create(null, TaskType.LEARN, "t1", null, 0, Map.of()));
    c1.addTask(PersonalTask.create(null, TaskType.LEARN, "t2", null, 1, Map.of()));
    quest.addChapter(c1);
    PersonalChapter c2 = PersonalChapter.create(null, "c2", null, 1);
    quest.addChapter(c2);
    return quest;
  }
}


