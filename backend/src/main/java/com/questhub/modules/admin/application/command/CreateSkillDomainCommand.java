package com.questhub.modules.admin.application.command;

/**
 * Command tạo mới Skill Domain (write request).
 *
 * @param name        tên skill domain
 * @param slug        slug duy nhất
 * @param description mô tả
 * @param icon        icon
 */
public record CreateSkillDomainCommand(String name, String slug, String description, String icon) {}
