package com.questhub.modules.marketplace.presentation.rest;

import com.questhub.modules.marketplace.application.command.CreateReviewCommand;
import com.questhub.modules.marketplace.application.command.UpdateReviewCommand;
import com.questhub.modules.marketplace.application.dto.FavoriteResponse;
import com.questhub.modules.marketplace.application.dto.MarketplaceHomeResponse;
import com.questhub.modules.marketplace.application.dto.MarketplaceHomeResponseMapper;
import com.questhub.modules.marketplace.application.dto.QuestCardResponse;
import com.questhub.modules.marketplace.application.dto.ReviewResponse;
import com.questhub.modules.marketplace.application.dto.SearchQuestResponse;
import com.questhub.modules.marketplace.application.query.GetMarketplaceLearningPathsQuery;
import com.questhub.modules.marketplace.application.query.PopularQuestsQuery;
import com.questhub.modules.marketplace.application.query.GetQuestReviewsQuery;
import com.questhub.modules.marketplace.application.query.TrendingQuestsQuery;
import com.questhub.modules.marketplace.application.query.GetUserFavoritesQuery;
import com.questhub.modules.marketplace.application.query.MarketplaceHomeQuery;
import com.questhub.modules.marketplace.application.query.SearchQuestsQuery;
import com.questhub.modules.marketplace.application.usecase.AddFavoriteUseCase;
import com.questhub.modules.marketplace.application.usecase.CreateReviewUseCase;
import com.questhub.modules.marketplace.application.usecase.DeleteReviewUseCase;
import com.questhub.modules.marketplace.application.usecase.RemoveFavoriteUseCase;
import com.questhub.modules.marketplace.application.usecase.UpdateReviewUseCase;
import com.questhub.shared.domain.AuthenticatedUser;
import com.questhub.shared.interfaces.dto.ApiResponse;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/marketplace")
@RequiredArgsConstructor
public class MarketplaceController {

  private final MarketplaceHomeQuery marketplaceHomeQuery;
  private final SearchQuestsQuery searchQuestsQuery;
  private final TrendingQuestsQuery getTrendingQuestsQuery;
  private final PopularQuestsQuery getPopularQuestsQuery;
  private final GetQuestReviewsQuery getQuestReviewsQuery;
  private final GetUserFavoritesQuery getUserFavoritesQuery;
  private final GetMarketplaceLearningPathsQuery getMarketplaceLearningPathsQuery;
  private final CreateReviewUseCase createReviewUseCase;
  private final UpdateReviewUseCase updateReviewUseCase;
  private final DeleteReviewUseCase deleteReviewUseCase;
  private final AddFavoriteUseCase addFavoriteUseCase;
  private final RemoveFavoriteUseCase removeFavoriteUseCase;

  @GetMapping("/home")
  public ResponseEntity<ApiResponse<MarketplaceHomeResponse>> home() {
    MarketplaceHomeQuery.HomeResult result = marketplaceHomeQuery.get();
    MarketplaceHomeResponse response = MarketplaceHomeResponseMapper.toResponse(result);
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  @GetMapping("/quests")
  public ResponseEntity<ApiResponse<List<SearchQuestResponse>>> search(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) UUID domainId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int limit) {
    List<SearchQuestResponse> results =
        searchQuestsQuery.search(q, domainId, page, limit).stream()
            .map(SearchQuestResponse::from)
            .toList();
    return ResponseEntity.ok(ApiResponse.ok(results));
  }

  @GetMapping("/learning-paths")
  public ResponseEntity<ApiResponse<List<GetMarketplaceLearningPathsQuery.DomainPaths>>> learningPaths() {
    return ResponseEntity.ok(ApiResponse.ok(getMarketplaceLearningPathsQuery.get()));
  }

  @GetMapping("/trending")
  public ResponseEntity<ApiResponse<List<QuestCardResponse>>> trending(
      @RequestParam(defaultValue = "20") int limit) {
    List<QuestCardResponse> response =
        getTrendingQuestsQuery.get(java.time.Instant.now().minus(7, java.time.temporal.ChronoUnit.DAYS), limit).stream()
            .map(QuestCardResponse::from)
            .toList();
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  @GetMapping("/popular")
  public ResponseEntity<ApiResponse<List<QuestCardResponse>>> popular(
      @RequestParam(defaultValue = "20") int limit) {
    List<QuestCardResponse> response =
        getPopularQuestsQuery.get(limit).stream()
            .map(QuestCardResponse::from)
            .toList();
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  @GetMapping("/quests/{id}/reviews")
  public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviews(
      @PathVariable UUID id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int limit) {
    List<ReviewResponse> response =
        getQuestReviewsQuery.get(id, page, limit).stream()
            .map(ReviewResponse::from)
            .toList();
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  @PostMapping("/quests/{id}/reviews")
  public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
      @PathVariable UUID id, @Valid @RequestBody CreateReviewCommand request) {
    AuthenticatedUser current = currentUser();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok(ReviewResponse.from(createReviewUseCase.create(id, current.id(), request))));
  }

  @PutMapping("/quests/{id}/reviews/me")
  public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
      @PathVariable UUID id, @Valid @RequestBody UpdateReviewCommand request) {
    AuthenticatedUser current = currentUser();
    return ResponseEntity.ok(ApiResponse.ok(ReviewResponse.from(updateReviewUseCase.update(id, current.id(), request))));
  }

  @DeleteMapping("/quests/{id}/reviews/me")
  public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable UUID id) {
    AuthenticatedUser current = currentUser();
    deleteReviewUseCase.delete(id, current.id());
    return ResponseEntity.ok(ApiResponse.ok(null));
  }

  @PostMapping("/quests/{id}/favorite")
  public ResponseEntity<ApiResponse<FavoriteResponse>> addFavorite(@PathVariable UUID id) {
    AuthenticatedUser current = currentUser();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok(FavoriteResponse.from(addFavoriteUseCase.add(current.id(), id))));
  }

  @DeleteMapping("/quests/{id}/favorite")
  public ResponseEntity<ApiResponse<Void>> removeFavorite(@PathVariable UUID id) {
    AuthenticatedUser current = currentUser();
    removeFavoriteUseCase.remove(current.id(), id);
    return ResponseEntity.ok(ApiResponse.ok(null));
  }

  @GetMapping("/users/me/favorites")
  public ResponseEntity<ApiResponse<List<FavoriteResponse>>> myFavorites() {
    AuthenticatedUser current = currentUser();
    List<FavoriteResponse> response =
        getUserFavoritesQuery.get(current.id()).stream()
            .map(FavoriteResponse::from)
            .toList();
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  private AuthenticatedUser currentUser() {
    return (AuthenticatedUser)
        SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }
}
