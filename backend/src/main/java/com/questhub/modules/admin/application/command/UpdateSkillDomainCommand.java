package com.questhub.modules.admin.application.command;

import java.util.UUID;

/**
 * Command cập nhật Skill Domain (write request).
 *
 * @param id          id của skill domain cần cập nhật
 * @param name        tên skill domain
 * @param slug        slug duy nhất
 * @param description mô tả
 * @param icon        icon
 */
public record UpdateSkillDomainCommand(
    UUID id, String name, String slug, String description, String icon) {}
