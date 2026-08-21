package com.questhub.modules.quest.application.dto;

import java.util.UUID;

public record PersonalQuestSummaryDto(
    UUID id, UUID questId, String title, String status, int progress) {}
