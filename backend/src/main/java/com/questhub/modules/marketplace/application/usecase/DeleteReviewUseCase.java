package com.questhub.modules.marketplace.application.usecase;

import com.questhub.modules.marketplace.application.event.QuestRatedEventPublisher;
import com.questhub.modules.marketplace.domain.review.Review;
import com.questhub.modules.marketplace.domain.review.ReviewRepository;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class DeleteReviewUseCase {

  private final ReviewRepository reviewRepository;
  private final QuestRatedEventPublisher questRatedEventPublisher;

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public void delete(UUID questId, UUID userId) {
    Review review =
        reviewRepository.findByQuestId(questId).stream()
            .filter(r -> r.getUserId().equals(userId))
            .findFirst()
            .orElseThrow(
                () -> BusinessException.notFound(ErrorCodes.NOT_FOUND, "Không tìm thấy đánh giá"));

    int previousScore = review.getScore();
    reviewRepository.delete(questId, userId);
    questRatedEventPublisher.publish(questId, userId, 0, previousScore, "DELETED");
  }
}
