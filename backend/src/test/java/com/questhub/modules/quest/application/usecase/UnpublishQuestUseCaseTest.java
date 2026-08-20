package com.questhub.modules.quest.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.questhub.modules.quest.application.helper.QuestAcess;
import com.questhub.modules.quest.domain.quest.Chapter;
import com.questhub.modules.quest.domain.quest.Difficulty;
import com.questhub.modules.quest.domain.quest.Quest;
import com.questhub.modules.quest.domain.quest.QuestRepository;
import com.questhub.modules.quest.domain.quest.QuestVisibility;
import com.questhub.modules.quest.domain.quest.Task;
import com.questhub.modules.quest.domain.quest.TaskType;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UnpublishQuestUseCaseTest {

  @Mock private QuestAcess questAcess;
  @Mock private QuestRepository questRepository;

  @InjectMocks private UnpublishQuestUseCase useCase;

  @Test
  void unpublish_publicQuest_shouldRevertToDraft() {
    UUID creatorId = UUID.randomUUID();
    Quest quest = publicQuest(creatorId);
    when(questAcess.loadForWrite(quest.getId(), creatorId)).thenReturn(quest);
    when(questRepository.save(any(Quest.class))).thenAnswer(inv -> inv.getArgument(0));

    Quest result = useCase.unpublish(quest.getId(), creatorId);

    assertThat(result.getVisibility()).isEqualTo(QuestVisibility.DRAFT);
    assertThat(result.getPublishedAt()).isNull();
    verify(questRepository).save(quest);
  }

  @Test
  void unpublish_alreadyDraft_shouldBeIdempotentWithoutSave() {
    UUID creatorId = UUID.randomUUID();
    Quest quest = Quest.create(creatorId, null, "title", "desc", Difficulty.BEGINNER, Map.of());
    when(questAcess.loadForWrite(quest.getId(), creatorId)).thenReturn(quest);

    Quest result = useCase.unpublish(quest.getId(), creatorId);

    assertThat(result.getVisibility()).isEqualTo(QuestVisibility.DRAFT);
    verify(questRepository, never()).save(any());
  }

  @Test
  void unpublish_nonCreator_shouldThrowForbidden() {
    UUID questId = UUID.randomUUID();
    UUID actorId = UUID.randomUUID();
    when(questAcess.loadForWrite(questId, actorId))
        .thenThrow(BusinessException.forbidden(ErrorCodes.FORBIDDEN, "Chỉ creator mới sửa được quest"));

    BusinessException ex =
        catchThrowableOfType(() -> useCase.unpublish(questId, actorId), BusinessException.class);

    assertThat(ex.getCode()).isEqualTo(ErrorCodes.FORBIDDEN);
    verify(questRepository, never()).save(any());
  }

  private Quest publicQuest(UUID creatorId) {
    Quest quest = draftQuest(creatorId);
    quest.publish();
    return quest;
  }

  private Quest draftQuest(UUID creatorId) {
    Quest quest = Quest.create(creatorId, null, "title", "desc", Difficulty.BEGINNER, Map.of());
    Chapter chapter = Chapter.create("c1", "d1", 0);
    chapter.addTask(Task.create(TaskType.LEARN, "t1", "d1", 0, Map.of()));
    quest.addChapter(chapter);
    return quest;
  }
}