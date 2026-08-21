package com.questhub.modules.quest.application.command;

import com.questhub.modules.quest.domain.quest.CompletionRule;
import jakarta.validation.constraints.NotNull;

public record SetCompletionRuleCommand(@NotNull CompletionRule completionRule) {}


