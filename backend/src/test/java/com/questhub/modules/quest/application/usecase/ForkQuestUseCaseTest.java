package com.questhub.modules.quest.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.questhub.modules.identity.domain.user.DisplayName;
import com.questhub.modules.identity.domain.user.Email;
import com.questhub.modules.identity.domain.user.Role;
import com.questhub.modules.identity.domain.user.User;
import com.questhub.modules.identity.domain.user.UserRepository;
import com.questhub.modules.identity.domain.user.Username;
import com.questhub.modules.quest.domain.personalquest.PersonalQuest;
import com.questhub.modules.quest.domain.personalquest.PersonalQuestRepository;
import com.questhub.modules.quest.domain.quest.Chapter;
import com.questhub.modules.quest.domain.quest.CompletionRule;
import com.questhub.modules.quest.domain.quest.Difficulty;
import com.questhub.modules.quest.domain.quest.Quest;
import com.questhub.modules.quest.domain.quest.QuestRepository;
import com.questhub.modules.quest.domain.quest.QuestVisibility;
import com.questhub.modules.quest.domain.quest.Task;
import com.questhub.modules.quest.domain.quest.TaskType;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import com.questhub.shared.outbox.OutboxPublisher;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ForkQuestUseCaseTest {

  @Mock private QuestRepository questRepository;
  @Mock private PersonalQuestRepository personalQuestRepository;
  @Mock private UserRepository userRepository;
  @Mock private OutboxPublisher outboxPublisher;

  @InjectMocks private ForkQuestUseCase useCase;

  @Test
  void fork_publicQuest_shouldCopyTreeWithSnapshotAndPublishEvent() {
    UUID userId = UUID.randomUUID();
    Quest quest = publicQuest(userId);
    when(questRepository.findById(quest.getId())).thenReturn(Optional.of(quest));
    when(personalQuestRepository.existsByUserIdAndQuestId(userId, quest.getId())).thenReturn(false);
    when(personalQuestRepository.save(any(PersonalQuest.class)))
        .thenAnswer(inv -> inv.getArgument(0));
    when(userRepository.findById(userId))
        .thenReturn(Optional.of(user(userId, "jane_doe")));

    PersonalQuest forked = useCase.fork(quest.getId(), userId);

    assertThat(forked.getUserId()).isEqualTo(userId);
    assertThat(forked.getQuestId()).isEqualTo(quest.getId());
    assertThat(forked.getTitle()).isEqualTo(quest.getTitle());
    assertThat(forked.getCompletionRule()).isEqualTo(quest.getCompletionRule());
    assertThat(forked.getChapters()).hasSize(2);
    assertThat(forked.getChapters().get(0).getTasks()).hasSize(2);
    assertThat(forked.getChapters().get(1).getTasks()).hasSize(1);
    assertThat(forked.getChapters().get(0).getTasks().get(0).getSourceTaskId())
        .isEqualTo(quest.getChapters().get(0).getTasks().get(0).getId());
    assertThat(forked.getChapters().get(0).getTasks().get(1).getConfig())
        .containsEntry("minScore", 70);

    ArgumentCaptor<Map> payload = ArgumentCaptor.forClass(Map.class);
    verify(outboxPublisher)
        .publish(eq("Quest"), eq(quest.getId()), eq("quest.forked"), payload.capture());
    assertThat(payload.getValue()).containsEntry("questId", quest.getId().toString());
    assertThat(payload.getValue()).containsEntry("questTitle", quest.getTitle());
    assertThat(payload.getValue()).containsEntry("personalQuestId", forked.getId().toString());
    assertThat(payload.getValue()).containsEntry("userId", userId.toString());
    assertThat(payload.getValue()).containsEntry("username", "jane_doe");
    assertThat(payload.getValue()).containsEntry("learningPathId", null);
  }

  @Test
  void fork_draftQuest_shouldThrowForbiddenWithoutSave() {
    UUID userId = UUID.randomUUID();
    Quest quest = Quest.create(userId, null, "Draft", "desc", Difficulty.BEGINNER, Map.of());
    when(questRepository.findById(quest.getId())).thenReturn(Optional.of(quest));

    BusinessException ex =
        catchThrowableOfType(() -> useCase.fork(quest.getId(), userId), BusinessException.class);

    assertThat(ex.getCode()).isEqualTo(ErrorCodes.FORBIDDEN);
    verify(personalQuestRepository, never()).save(any());
    verify(outboxPublisher, never()).publish(any(), any(), any(), any());
  }

  @Test
  void fork_alreadyForked_shouldThrowConflictWithoutSave() {
    UUID userId = UUID.randomUUID();
    Quest quest = publicQuest(userId);
    when(questRepository.findById(quest.getId())).thenReturn(Optional.of(quest));
    when(personalQuestRepository.existsByUserIdAndQuestId(userId, quest.getId())).thenReturn(true);

    BusinessException ex =
        catchThrowableOfType(() -> useCase.fork(quest.getId(), userId), BusinessException.class);

    assertThat(ex.getCode()).isEqualTo(ErrorCodes.CONFLICT);
    verify(personalQuestRepository, never()).save(any());
    verify(outboxPublisher, never()).publish(any(), any(), any(), any());
  }

  @Test
  void fork_unknownQuest_shouldThrowNotFound() {
    UUID questId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    when(questRepository.findById(questId)).thenReturn(Optional.empty());

    BusinessException ex =
        catchThrowableOfType(() -> useCase.fork(questId, userId), BusinessException.class);

    assertThat(ex.getCode()).isEqualTo(ErrorCodes.NOT_FOUND);
    verify(personalQuestRepository, never()).save(any());
    verify(outboxPublisher, never()).publish(any(), any(), any(), any());
  }

  @Test
  void fork_snapshotIsIndependentOfOriginalChanges() {
    UUID userId = UUID.randomUUID();
    Quest quest = publicQuest(userId);
    when(questRepository.findById(quest.getId())).thenReturn(Optional.of(quest));
    when(personalQuestRepository.existsByUserIdAndQuestId(userId, quest.getId())).thenReturn(false);
    when(personalQuestRepository.save(any(PersonalQuest.class)))
        .thenAnswer(inv -> inv.getArgument(0));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user(userId, "jane_doe")));

    PersonalQuest forked = useCase.fork(quest.getId(), userId);

    quest.updateMetadata("Title mới", "desc", Difficulty.ADVANCED,
        CompletionRule.defaultAllTasks(), Map.of());

    assertThat(forked.getTitle()).isEqualTo("Spring Security Fundamentals");
    assertThat(forked.getCompletionRule().minScore()).isEqualByComparingTo(new BigDecimal("70"));
  }

  private Quest publicQuest(UUID creatorId) {
    Quest quest =
        Quest.create(
            creatorId,
            null,
            "Spring Security Fundamentals",
            "Học Spring Security",
            Difficulty.INTERMEDIATE,
            Map.of("icon", "x"));
    quest.setCompletionRule(CompletionRule.quizScore(new BigDecimal("70")));
    Chapter c1 = Chapter.create("Authentication", "Phần 1", 0);
    c1.addTask(Task.create(TaskType.LEARN, "Xem video", "video", 0, Map.of()));
    c1.addTask(Task.create(TaskType.QUIZ, "Quiz 1", "quiz", 1, Map.of("minScore", 70)));
    quest.addChapter(c1);
    Chapter c2 = Chapter.create("Authorization", "Phần 2", 1);
    c2.addTask(Task.create(TaskType.REFLECTION, "Reflection", "ref", 0, Map.of()));
    quest.addChapter(c2);
    quest.publish();
    return quest;
  }

  private User user(UUID id, String username) {
    return User.restore(
        id,
        new Email("jane@example.com"),
        new Username(username),
        new DisplayName("Jane"),
        "$2a$10$hash",
        Role.USER,
        null,
        null,
        true,
        0,
        0,
        Map.of(),
        Instant.now(),
        Instant.now());
  }
}