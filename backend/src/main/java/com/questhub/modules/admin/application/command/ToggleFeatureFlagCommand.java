package com.questhub.modules.admin.application.command;

import java.util.Map;

/**
 * Command bật/tắt feature flag (write request).
 *
 * @param key         key của feature flag
 * @param value       giá trị flag (JSON object tuỳ ý)
 * @param description mô tả (tuỳ chọn, chỉ áp dụng khi tạo mới/cập nhật)
 */
public record ToggleFeatureFlagCommand(String key, Map<String, Object> value, String description) {}
