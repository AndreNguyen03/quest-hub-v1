package com.questhub.modules.quest.application.command;

import java.util.List;
import java.util.UUID;

public record ReorderTasksCommand(List<UUID> taskIds) {}
