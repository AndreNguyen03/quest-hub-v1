package com.questhub.modules.identity.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class DisplayNameTest {

  @Test
  void trimsWhitespace() {
    assertThat(new DisplayName("  Minh Nguyen  ").value()).isEqualTo("Minh Nguyen");
  }

  @Test
  void acceptsVietnameseLetters() {
    assertThat(new DisplayName("Nguyễn Minh Quân").value()).isEqualTo("Nguyễn Minh Quân");
  }

  @Test
  void rejectsDigitsAndUnderscore() {
    assertThatThrownBy(() -> new DisplayName("John2")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new DisplayName("john_doe"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void rejectsBlank() {
    assertThatThrownBy(() -> new DisplayName("")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new DisplayName("   ")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new DisplayName(null)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void enforcesMaxLength() {
    assertThat(new DisplayName("a".repeat(100)).value()).hasSize(100);
    assertThatThrownBy(() -> new DisplayName("a".repeat(101)))
        .isInstanceOf(IllegalArgumentException.class);
  }
}