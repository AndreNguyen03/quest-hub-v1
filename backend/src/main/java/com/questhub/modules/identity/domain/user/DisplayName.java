package com.questhub.modules.identity.domain.user;

import com.questhub.shared.domain.DomainValidationException;

public record DisplayName(String value) {

  private static final int MAX_LENGTH = 100;

  public DisplayName {
    if (value == null) {
      throw new DomainValidationException("Display name không được để trống");
    }
    value = value.trim();
    if (value.isEmpty() || value.length() > MAX_LENGTH || !value.matches("^[\\p{L} ]+$")) {
      throw new DomainValidationException("Display name chỉ gồm chữ cái và dấu cách, tối đa 100 ký tự");
    }
  }
}
