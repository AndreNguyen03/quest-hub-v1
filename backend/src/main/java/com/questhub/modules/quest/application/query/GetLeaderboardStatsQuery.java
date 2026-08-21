package com.questhub.modules.quest.application.query;

import com.questhub.modules.quest.application.dto.LeaderboardStatDto;
import com.questhub.modules.quest.domain.personalquest.PersonalQuestRepository;
import com.questhub.shared.annotation.UseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class GetLeaderboardStatsQuery {

  private final PersonalQuestRepository personalQuestRepository;

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public List<LeaderboardStatDto> top(int limit) {
    return personalQuestRepository.topByCompletionStats(limit).stream()
        .map(s -> new LeaderboardStatDto(s.userId(), s.questCount(), s.taskCount()))
        .toList();
  }
}
