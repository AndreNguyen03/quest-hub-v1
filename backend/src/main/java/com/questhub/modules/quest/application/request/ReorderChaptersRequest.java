package com.questhub.modules.quest.application.request;

import java.util.List;
import java.util.UUID;

public record ReorderChaptersRequest(List<UUID> chapterIds) {}