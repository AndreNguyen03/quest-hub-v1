package com.questhub.modules.marketplace.application.query;

import com.questhub.modules.quest.application.dto.LearningPathDto;
import com.questhub.modules.quest.application.dto.SkillDomainDto;
import com.questhub.modules.quest.application.query.ListPublicLearningPathsQuery;
import com.questhub.modules.quest.application.query.ListSkillDomainsQuery;
import com.questhub.shared.annotation.UseCase;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class GetMarketplaceLearningPathsQuery {

  private final ListSkillDomainsQuery listSkillDomainsQuery;
  private final ListPublicLearningPathsQuery listPublicLearningPathsQuery;

  public record DomainPaths(UUID domainId, String domainName, String slug, List<PathDto> paths) {}

  public record PathDto(UUID id, String title, String description, String difficulty) {}

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public List<DomainPaths> get() {
    List<SkillDomainDto> domains = listSkillDomainsQuery.list();
    List<LearningPathDto> paths = listPublicLearningPathsQuery.list();

    return domains.stream()
        .map(
            domain -> {
              List<PathDto> domainPaths =
                  paths.stream()
                      .filter(p -> p.domainId().equals(domain.id()))
                      .map(
                          p ->
                              new PathDto(
                                  p.id(), p.title(), p.description(), p.difficulty()))
                      .toList();
              return new DomainPaths(domain.id(), domain.name(), domain.slug(), domainPaths);
            })
        .filter(dp -> !dp.paths().isEmpty())
        .toList();
  }
}
