package com.questhub.modules.quest.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.questhub.modules.quest.application.command.CreateLearningPathCommand;
import com.questhub.modules.quest.domain.learningpath.LearningPath;
import com.questhub.modules.quest.domain.learningpath.LearningPathRepository;
import com.questhub.modules.quest.domain.quest.Difficulty;
import com.questhub.modules.quest.domain.skilldomain.SkillDomainRepository;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.questhub.shared.domain.ResponseStatus;

@ExtendWith(MockitoExtension.class)
class CreateLearningPathUseCaseTest {

  @Mock private SkillDomainRepository skillDomainRepository;
  @Mock private LearningPathRepository learningPathRepository;

  @InjectMocks private CreateLearningPathUseCase useCase;

  @Test
  void create_whenDomainExists_shouldCreatePrivatePathAndSave() {
    UUID authorId = UUID.randomUUID();
    UUID domainId = UUID.randomUUID();
    when(skillDomainRepository.existsById(domainId)).thenReturn(true);
    when(learningPathRepository.save(any(LearningPath.class))).thenAnswer(inv -> inv.getArgument(0));

    LearningPath created = useCase.create(authorId, request(domainId));

    assertThat(created).isNotNull();
    assertThat(created.getId()).isNotNull();
    assertThat(created.getAuthorId()).isEqualTo(authorId);
    assertThat(created.getDomainId()).isEqualTo(domainId);
    assertThat(created.getTitle()).isEqualTo("Java Backend Engineer");
    assertThat(created.getDifficulty()).isEqualTo(Difficulty.INTERMEDIATE);
    assertThat(created.isPublic()).isFalse();

    ArgumentCaptor<LearningPath> saved = ArgumentCaptor.forClass(LearningPath.class);
    verify(learningPathRepository).save(saved.capture());
    assertThat(saved.getValue().getAuthorId()).isEqualTo(authorId);
  }

  @Test
  void create_whenDomainMissing_shouldThrowNotFoundAndNotSave() {
    UUID domainId = UUID.randomUUID();
    when(skillDomainRepository.existsById(domainId)).thenReturn(false);

    BusinessException ex =
        catchThrowableOfType(
            () -> useCase.create(UUID.randomUUID(), request(domainId)), BusinessException.class);

    assertThat(ex.getCode()).isEqualTo(ErrorCodes.NOT_FOUND);
    assertThat(ex.getStatus()).isEqualTo(ResponseStatus.NOT_FOUND);
    verify(learningPathRepository, never()).save(any());
  }

  private CreateLearningPathCommand request(UUID domainId) {
    return new CreateLearningPathCommand(
        "Java Backend Engineer", "Roadmap Java", domainId, Difficulty.INTERMEDIATE);
  }
}



