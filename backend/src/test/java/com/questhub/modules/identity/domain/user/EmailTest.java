package com.questhub.modules.identity.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EmailTest {

  @Test
  void acceptsValidLowercaseEmails() {
    assertThat(new Email("alice@example.com").value()).isEqualTo("alice@example.com");
    assertThat(new Email("a.b+c%tag@sub.example.io").value()).isEqualTo("a.b+c%tag@sub.example.io");
  }

  @Test
  void rejectsUppercase() {
    assertThatThrownBy(() -> new Email("Alice@example.com"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void rejectsMalformed() {
    assertThatThrownBy(() -> new Email("alice@"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Email("alice.example.com"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Email("alice@example"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void rejectsBlankOrNull() {
    assertThatThrownBy(() -> new Email("")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Email("   ")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Email(null)).isInstanceOf(IllegalArgumentException.class);
  }
}