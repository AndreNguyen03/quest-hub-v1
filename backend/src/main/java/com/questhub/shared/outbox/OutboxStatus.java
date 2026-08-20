package com.questhub.shared.outbox;

public enum OutboxStatus {
  PENDING,
  PROCESSING,
  PROCESSED,
  FAILED
}