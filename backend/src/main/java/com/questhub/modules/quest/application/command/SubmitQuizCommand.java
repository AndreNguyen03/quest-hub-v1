package com.questhub.modules.quest.application.command;

import java.util.Map;

public record SubmitQuizCommand(Map<String, Object> answers) {}
