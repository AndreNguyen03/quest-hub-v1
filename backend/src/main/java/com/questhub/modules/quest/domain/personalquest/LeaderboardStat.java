package com.questhub.modules.quest.domain.personalquest;

import java.util.UUID;

public record LeaderboardStat(UUID userId, long questCount, long taskCount) {}
