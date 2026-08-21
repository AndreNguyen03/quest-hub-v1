package com.questhub.modules.world.application.query;

import com.questhub.modules.identity.application.query.GetUsernameQuery;
import com.questhub.modules.quest.application.dto.LeaderboardStatDto;
import com.questhub.modules.quest.application.query.GetLeaderboardStatsQuery;
import com.questhub.shared.annotation.UseCase;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class GetLeaderboardQuery {

  private static final Logger log = LoggerFactory.getLogger(GetLeaderboardQuery.class);

  private final GetLeaderboardStatsQuery getLeaderboardStatsQuery;
  private final GetUsernameQuery getUsernameQuery;

  public record LeaderboardEntry(UUID userId, String username, long questCount, long taskCount) {}

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public List<LeaderboardEntry> get(int limit) {
    log.info("Get leaderboard limit={}", limit);
    List<LeaderboardStatDto> stats = getLeaderboardStatsQuery.top(limit);
    Map<UUID, String> usernames =
        getUsernameQuery.byIds(stats.stream().map(LeaderboardStatDto::userId).toList());
    return stats.stream()
        .map(
            s ->
                new LeaderboardEntry(
                    s.userId(), usernames.get(s.userId()), s.questCount(), s.taskCount()))
        .toList();
  }
}

