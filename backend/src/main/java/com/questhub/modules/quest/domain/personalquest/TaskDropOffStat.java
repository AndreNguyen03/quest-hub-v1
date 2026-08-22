package com.questhub.modules.quest.domain.personalquest;

import java.util.UUID;

public record TaskDropOffStat(UUID sourceTaskId, long completedCount, long totalCount) {}
