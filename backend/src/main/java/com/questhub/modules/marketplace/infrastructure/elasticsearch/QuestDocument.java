package com.questhub.modules.marketplace.infrastructure.elasticsearch;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "quests")
public class QuestDocument {

  @Id
  private UUID id;

  @Field(type = FieldType.Text, fielddata = true)
  private String title;

  @Field(type = FieldType.Text)
  private String description;

  @Field(type = FieldType.Keyword)
  private String difficulty;

  @Field(type = FieldType.Keyword)
  private UUID learningPathId;

  @Field(type = FieldType.Keyword)
  private UUID domainId;

  @Field(type = FieldType.Integer)
  private int forkCount;

  @Field(type = FieldType.Double)
  private BigDecimal avgRating;

  @Field(type = FieldType.Integer)
  private int ratingCount;

  @Field(type = FieldType.Date)
  private Instant publishedAt;

  protected QuestDocument() {}

  public QuestDocument(
      UUID id,
      String title,
      String description,
      String difficulty,
      UUID learningPathId,
      UUID domainId,
      int forkCount,
      BigDecimal avgRating,
      int ratingCount,
      Instant publishedAt) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.difficulty = difficulty;
    this.learningPathId = learningPathId;
    this.domainId = domainId;
    this.forkCount = forkCount;
    this.avgRating = avgRating;
    this.ratingCount = ratingCount;
    this.publishedAt = publishedAt;
  }

  public UUID getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public String getDifficulty() {
    return difficulty;
  }

  public UUID getLearningPathId() {
    return learningPathId;
  }

  public UUID getDomainId() {
    return domainId;
  }

  public int getForkCount() {
    return forkCount;
  }

  public BigDecimal getAvgRating() {
    return avgRating;
  }

  public int getRatingCount() {
    return ratingCount;
  }

  public Instant getPublishedAt() {
    return publishedAt;
  }
}
