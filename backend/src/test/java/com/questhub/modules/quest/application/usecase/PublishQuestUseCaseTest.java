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
import com.questhub.modules.quest.application.helper.QuestAcess;
import com.questhub.modules.quest.domain.learningpath.LearningPathRepository;
import com.questhub.modules.quest.domain.quest.Chapter;
import com.questhub.modules.quest.domain.quest.Difficulty;
import com.questhub.modules.quest.domain.quest.Quest;
import com.questhub.modules.quest.domain.quest.QuestRepository;
import com.questhub.modules.quest.domain.quest.QuestVisibility;
import com.questhub.modules.quest.domain.quest.Task;
import com.questhub.modules.quest.domain.quest.TaskType;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.DomainValidationException;
import com.questhub.shared.domain.ErrorCodes;
import com.questhub.shared.outbox.OutboxPublisher;
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
class PublishQuestUseCaseTest {

  @Mock private QuestAcess questAcess;
  @Mock private QuestRepository questRepository;
  @Mock private LearningPathRepository learningPathRepository;
  @Mock private UserRepository userRepository;
  @Mock private OutboxPublisher outboxPublisher;

  @InjectMocks private PublishQuestUseCase useCase;

  @Test
  void publish_validTree_shouldSetPublicWriteOutboxAndPromoteCreator() {
    UUID creatorId = UUID.randomUUID();
    Quest quest = draftQuest(creatorId);
    when(questAcess.loadForWrite(quest.getId(), creatorId)).thenReturn(quest);
    when(questRepository.existsByCreatorIdAndVisibility(creatorId, QuestVisibility.PUBLIC))
        .thenReturn(false);
    when(questRepository.save(any(Quest.class))).thenAnswer(inv -> inv.getArgument(0));
    when(userRepository.findById(creatorId)).thenReturn(Optional.of(user(Role.USER)));
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    Quest published = useCase.publish(quest.getId(), creatorId);

    assertThat(published.getVisibility()).isEqualTo(QuestVisibility.PUBLIC);
    assertThat(published.getPublishedAt()).isNotNull();

    ArgumentCaptor<Map> payload = ArgumentCaptor.forClass(Map.class);
    verify(outboxPublisher)
        .publish(eq("Quest"), eq(quest.getId()), eq("quest.published"), payload.capture());
    assertThat(payload.getValue()).containsEntry("questId", quest.getId().toString());
    assertThat(payload.getValue()).containsEntry("taskCount", 1);
    assertThat(payload.getValue()).containsEntry("creatorUsername", "binh_nguyen");

    verify(userRepository).save(any(User.class));
  }

  @Test
  void publish_whenCreatorAlreadyHasPublicQuest_shouldNotPromoteAgain() {
    UUID creatorId = UUID.randomUUID();
    Quest quest = draftQuest(creatorId);
    when(questAcess.loadForWrite(quest.getId(), creatorId)).thenReturn(quest);
    when(questRepository.existsByCreatorIdAndVisibility(creatorId, QuestVisibility.PUBLIC))
        .thenReturn(true);
    when(questRepository.save(any(Quest.class))).thenAnswer(inv -> inv.getArgument(0));

    Quest published = useCase.publish(quest.getId(), creatorId);

    assertThat(published.getVisibility()).isEqualTo(QuestVisibility.PUBLIC);
    verify(userRepository, never()).save(any());
  }

  @Test
  void publish_emptyQuest_shouldThrowDomainValidationAndNotSave() {
    UUID creatorId = UUID.randomUUID();
    Quest quest = Quest.create(creatorId, null, "Quest rỗng", "desc", Difficulty.BEGINNER, Map.of());
    when(questAcess.loadForWrite(quest.getId(), creatorId)).thenReturn(quest);

    DomainValidationException ex =
        catchThrowableOfType(
            () -> useCase.publish(quest.getId(), creatorId), DomainValidationException.class);

    assertThat(ex.getMessage()).contains("at least one chapter");
    verify(questRepository, never()).save(any());
    verify(outboxPublisher, never()).publish(any(), any(), any(), any());
  }

  @Test
  void publish_alreadyPublic_shouldBeIdempotentWithoutSaveOrOutbox() {
    UUID creatorId = UUID.randomUUID();
    Quest quest = draftQuest(creatorId);
    quest.publish();
    when(questAcess.loadForWrite(quest.getId(), creatorId)).thenReturn(quest);

    Quest published = useCase.publish(quest.getId(), creatorId);

    assertThat(published.getVisibility()).isEqualTo(QuestVisibility.PUBLIC);
    verify(questRepository, never()).save(any());
    verify(outboxPublisher, never()).publish(any(), any(), any(), any());
  }

  @Test
  void publish_nonCreator_shouldThrowForbidden() {
    UUID questId = UUID.randomUUID();
    UUID actorId = UUID.randomUUID();
    when(questAcess.loadForWrite(questId, actorId))
        .thenThrow(BusinessException.forbidden(ErrorCodes.FORBIDDEN, "Chỉ creator mới sửa được quest"));

    BusinessException ex =
        catchThrowableOfType(() -> useCase.publish(questId, actorId), BusinessException.class);

    assertThat(ex.getCode()).isEqualTo(ErrorCodes.FORBIDDEN);
    verify(questRepository, never()).save(any());
    verify(outboxPublisher, never()).publish(any(), any(), any(), any());
  }

  private Quest draftQuest(UUID creatorId) {
    Quest quest =
        Quest.create(creatorId, null, "Spring Security Fundamentals", "Học Spring Security",
            Difficulty.INTERMEDIATE, Map.of("icon", "x"));
    Chapter chapter = Chapter.create("Authentication", "Phần 1", 0);
    chapter.addTask(Task.create(TaskType.LEARN, "Xem video", "video", 0, Map.of()));
    quest.addChapter(chapter);
    return quest;
  }

  private User user(Role role) {
    return User.restore(
        UUID.randomUUID(),
        new Email("binh@example.com"),
        new Username("binh_nguyen"),
        new DisplayName("Binh"),
        "$2a$10$hash",
        role,
        null,
        null,
        true,
        0,
        0,
        Map.of(),
        java.time.Instant.now(),
        java.time.Instant.now());
  }
}