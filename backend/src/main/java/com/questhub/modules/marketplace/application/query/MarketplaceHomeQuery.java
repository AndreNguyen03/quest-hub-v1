package com.questhub.modules.marketplace.application.query;

import com.questhub.modules.quest.application.dto.LearningPathDto;
import com.questhub.modules.quest.application.dto.QuestDto;
import com.questhub.modules.quest.application.dto.SkillDomainDto;
import com.questhub.modules.quest.application.query.GetPopularQuestsQuery;
import com.questhub.modules.quest.application.query.GetTrendingQuestsQuery;
import com.questhub.modules.quest.application.query.ListPublicLearningPathsQuery;
import com.questhub.modules.quest.application.query.ListSkillDomainsQuery;
import com.questhub.shared.annotation.UseCase;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class MarketplaceHomeQuery {

  private final ListSkillDomainsQuery listSkillDomainsQuery;
  private final ListPublicLearningPathsQuery listPublicLearningPathsQuery;
  private final GetPopularQuestsQuery getPopularQuestsQuery;
  private final GetTrendingQuestsQuery getTrendingQuestsQuery;

  public record QuestCard(UUID id, String title, String description, String difficulty, UUID domainId, int forkCount, Double avgRating) {}
  public record PathCard(UUID id, String title, String description, String difficulty, UUID domainId) {}
  public record DomainGroup(UUID domainId, String domainName, String slug, List<PathCard> paths) {}
  public record HomeResult(List<DomainGroup> pathsByDomain, List<QuestCard> popular, List<QuestCard> trending) {}

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public HomeResult get() {
    List<SkillDomainDto> domains = listSkillDomainsQuery.list();
    List<LearningPathDto> publicPaths = listPublicLearningPathsQuery.list();

    Map<UUID, List<LearningPathDto>> pathsByDomainId =
        publicPaths.stream().collect(Collectors.groupingBy(LearningPathDto::domainId));

    Map<UUID, UUID> learningPathIdToDomainId =
        publicPaths.stream()
            .collect(Collectors.toMap(LearningPathDto::id, LearningPathDto::domainId));

    List<DomainGroup> pathsByDomain =
        domains.stream()
            .map(domain -> {
              List<PathCard> cards =
                  pathsByDomainId.getOrDefault(domain.id(), List.of()).stream()
                      .map(MarketplaceHomeMapper::toPathCard)
                      .toList();
              return new DomainGroup(domain.id(), domain.name(), domain.slug(), cards);
            })
            .filter(g -> !g.paths().isEmpty())
            .toList();

    List<QuestDto> popularEntities = getPopularQuestsQuery.get(20);
    List<QuestCard> popular =
        popularEntities.stream()
            .map(q -> toQuestCard(q, learningPathIdToDomainId))
            .toList();

    Instant since = Instant.now().minus(7, ChronoUnit.DAYS);
    List<QuestDto> trendingEntities = getTrendingQuestsQuery.get(since, 20);
    List<QuestCard> trending =
        trendingEntities.stream()
            .map(q -> toQuestCard(q, learningPathIdToDomainId))
            .toList();
    if (trending.isEmpty()) {
      trending = popular.stream().limit(10).toList();
    }

    return new HomeResult(pathsByDomain, popular, trending);
  }

  private QuestCard toQuestCard(QuestDto q, Map<UUID, UUID> learningPathIdToDomainId) {
    UUID domainId = null;
    if (q.learningPathId() != null) {
      domainId = learningPathIdToDomainId.get(q.learningPathId());
    }
    Double avgRating = null;
    if (q.avgRating() != null) {
      avgRating = q.avgRating().doubleValue();
    }
    return new QuestCard(
        q.id(),
        q.title(),
        q.description(),
        q.difficulty(),
        domainId,
        q.forkCount(),
        avgRating);
  }
}







