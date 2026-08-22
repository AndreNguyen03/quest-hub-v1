package com.questhub.modules.marketplace.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.questhub.modules.marketplace.domain.favorite.Favorite;
import com.questhub.modules.marketplace.domain.favorite.FavoriteRepository;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AddFavoriteUseCaseTest {

  @Mock private FavoriteRepository favoriteRepository;

  @InjectMocks private AddFavoriteUseCase useCase;

  @Test
  void add_newFavorite_shouldSaveAndReturn() {
    UUID userId = UUID.randomUUID();
    UUID questId = UUID.randomUUID();

    when(favoriteRepository.existsByUserIdAndQuestId(userId, questId)).thenReturn(false);
    when(favoriteRepository.save(any(Favorite.class))).thenAnswer(inv -> inv.getArgument(0));

    Favorite result = useCase.add(userId, questId);

    assertThat(result.getUserId()).isEqualTo(userId);
    assertThat(result.getQuestId()).isEqualTo(questId);
    verify(favoriteRepository).save(any(Favorite.class));
  }

  @Test
  void add_duplicate_shouldThrowConflict() {
    UUID userId = UUID.randomUUID();
    UUID questId = UUID.randomUUID();

    when(favoriteRepository.existsByUserIdAndQuestId(userId, questId)).thenReturn(true);

    BusinessException ex = catchThrowableOfType(
        () -> useCase.add(userId, questId),
        BusinessException.class);

    assertThat(ex.getCode()).isEqualTo(ErrorCodes.CONFLICT);
    verify(favoriteRepository, never()).save(any());
  }
}
