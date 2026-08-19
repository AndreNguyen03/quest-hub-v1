package com.questhub.shared.domain;

public class DomainValidationException extends IllegalArgumentException {

  public DomainValidationException(String message) {
    super(message);
  }
}