package com.questhub.modules.quest.interfaces.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class QuestFlowIntegrationTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Container
  @ServiceConnection
  static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

  @Autowired private TestRestTemplate rest;
  @Autowired private JdbcTemplate jdbc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void createQuest_withNestedTree_shouldReturn201AndPersistAllLevels() throws Exception {
    String token = registerAndGetToken("binh@example.com", "binh_nguyen");

    Map<String, Object> payload =
        Map.of(
            "title", "Spring Security Fundamentals",
            "description", "Học Spring Security",
            "difficulty", "INTERMEDIATE",
            "completionRule", Map.of("type", "ALL_TASKS"),
            "chapters",
                List.of(
                    Map.of(
                        "title", "Authentication",
                        "description", "Phần 1",
                        "tasks",
                            List.of(
                                Map.of(
                                    "type", "LEARN",
                                    "title", "Xem video",
                                    "description", "video JWT",
                                    "config", Map.of()),
                                Map.of(
                                    "type", "PRACTICE",
                                    "title", "Code login",
                                    "description", "controller",
                                    "config", Map.of("passThreshold", 80)))),
                    Map.of(
                        "title", "Authorization",
                        "description", "Phần 2",
                        "tasks",
                            List.of(
                                Map.of(
                                    "type", "QUIZ",
                                    "title", "Quiz role",
                                    "description", "trắc nghiệm",
                                    "config", Map.of("passThreshold", 80))))));

    ResponseEntity<String> response =
        rest.exchange(
            "/api/v1/quests",
            HttpMethod.POST,
            jsonEntity(token, payload),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    JsonNode body = objectMapper.readTree(response.getBody());
    assertThat(body.path("data").path("visibility").asText()).isEqualTo("DRAFT");
    assertThat(body.path("data").path("chapters")).hasSize(2);
    assertThat(body.path("data").path("chapters").get(0).path("position").asInt()).isEqualTo(0);
    assertThat(body.path("data").path("chapters").get(0).path("tasks")).hasSize(2);
    assertThat(body.path("data").path("chapters").get(0).path("tasks").get(1).path("order").asInt())
        .isEqualTo(1);

    String questId = body.path("data").path("id").asText();
    Integer quests =
        jdbc.queryForObject(
            "SELECT count(*) FROM quests WHERE id = ? AND visibility = 'DRAFT'",
            Integer.class,
            UUID.fromString(questId));
    assertThat(quests).isEqualTo(1);
    Integer chapters =
        jdbc.queryForObject(
            "SELECT count(*) FROM chapters WHERE quest_id = ?", Integer.class, UUID.fromString(questId));
    assertThat(chapters).isEqualTo(2);
    Integer tasks =
        jdbc.queryForObject(
            "SELECT count(*) FROM tasks WHERE chapter_id IN "
                + "(SELECT id FROM chapters WHERE quest_id = ?)",
            Integer.class,
            UUID.fromString(questId));
    assertThat(tasks).isEqualTo(3);
  }

  @Test
  void createQuest_emptyChapters_shouldReturn400ValidationError() throws Exception {
    String token = registerAndGetToken("chi@example.com", "chi_nguyen");

    Map<String, Object> payload =
        Map.of(
            "title", "Quest rỗng",
            "difficulty", "BEGINNER",
            "chapters", List.of());

    ResponseEntity<String> response =
        rest.exchange("/api/v1/quests", HttpMethod.POST, jsonEntity(token, payload), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void getQuest_ownDraft_shouldReturn200() throws Exception {
    String token = registerAndGetToken("anh@example.com", "anh_nguyen");
    String questId = createQuest(token);

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);

    ResponseEntity<String> response =
        rest.exchange(
            "/api/v1/quests/" + questId, HttpMethod.GET, new HttpEntity<>(headers), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode body = objectMapper.readTree(response.getBody());
    assertThat(body.path("data").path("title").asText()).isEqualTo("Spring Security Fundamentals");
    assertThat(body.path("data").path("chapters").get(0).path("tasks")).hasSize(2);
  }

  @Test
  void getQuest_otherUsersDraft_shouldReturn403() throws Exception {
    String ownerToken = registerAndGetToken("dung@example.com", "dung_nguyen");
    String questId = createQuest(ownerToken);
    String otherToken = registerAndGetToken("phuong@example.com", "phuong_nguyen");

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(otherToken);

    ResponseEntity<String> response =
        rest.exchange(
            "/api/v1/quests/" + questId, HttpMethod.GET, new HttpEntity<>(headers), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  private String registerAndGetToken(String email, String username) throws Exception {
    ResponseEntity<String> response =
        rest.postForEntity(
            "/api/v1/auth/register",
            Map.of(
                "email", email,
                "username", username,
                "displayName", "Test User",
                "password", "matkhau123"),
            String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    JsonNode body = objectMapper.readTree(response.getBody());
    return body.path("data").path("accessToken").asText();
  }

  private String createQuest(String token) {
    Map<String, Object> payload =
        Map.of(
            "title", "Spring Security Fundamentals",
            "description", "Học Spring Security",
            "difficulty", "INTERMEDIATE",
            "chapters",
                List.of(
                    Map.of(
                        "title", "Authentication",
                        "description", "Phần 1",
                        "tasks",
                            List.of(
                                Map.of(
                                    "type", "LEARN", "title", "Xem video", "description", "v1", "config", Map.of()),
                                Map.of(
                                    "type", "PRACTICE", "title", "Code login", "description", "v2", "config", Map.of())))));
    ResponseEntity<String> response =
        rest.exchange("/api/v1/quests", HttpMethod.POST, jsonEntity(token, payload), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    try {
      JsonNode body = objectMapper.readTree(response.getBody());
      return body.path("data").path("id").asText();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private HttpEntity<Map<String, Object>> jsonEntity(String token, Map<String, Object> body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(token);
    return new HttpEntity<>(body, headers);
  }
}