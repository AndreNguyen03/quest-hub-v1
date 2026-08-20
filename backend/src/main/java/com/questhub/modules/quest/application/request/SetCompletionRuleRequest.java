package com.questhub.modules.quest.application.request;

import com.questhub.modules.quest.domain.quest.CompletionRule;
import jakarta.validation.constraints.NotNull;

public record SetCompletionRuleRequest(@NotNull CompletionRule completionRule) {}