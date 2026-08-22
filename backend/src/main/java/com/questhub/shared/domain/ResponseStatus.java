package com.questhub.shared.domain;

/**
 * Semantic HTTP-like response status owned by the domain layer.
 * Presentation maps this to the framework's HTTP status type.
 */
public enum ResponseStatus {
  BAD_REQUEST,
  UNAUTHORIZED,
  FORBIDDEN,
  NOT_FOUND,
  CONFLICT
}
