package com.questhub.modules.identity.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class UsernameTest {

  @Test
  void acceptsLowercaseLettersDigitsUnderscore() {
    assertThat(new Username("alice").value()).isEqualTo("alice");
    assertThat(new Username("alice_01").value()).isEqualTo("alice_01");
    assertThat(new Username("_alice").value()).isEqualTo("_alice");
  }

  @Test
  void rejectsUppercase() {
    assertThatThrownBy(() -> new Username("Alice")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void rejectsSpacesAndSpecialChars() {
    assertThatThrownBy(() -> new Username("alice name"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Username("alice-name"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Username("alice!"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void rejectsBlankOrNull() {
    assertThatThrownBy(() -> new Username("")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Username(null)).isInstanceOf(IllegalArgumentException.class);
  }
}