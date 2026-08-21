package com.questhub.modules.world.presentation.rest;

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
class WorldFlowIntegrationTest {

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
  void world_me_shouldReflectTaskCompletionsAndUndo() throws Exception {
    String email = "world_test@example.com";
    String token = registerAndGetToken(email, "world_test_user");
    UUID userId = userIdByEmail(email);
    UUID domainId = UUID.randomUUID();
    UUID learningPathId = UUID.randomUUID();
    jdbc.update(
        "INSERT INTO skill_domains (id, name, slug, description, icon) VALUES (?, ?, ?, ?, ?)",
        domainId, "Java Backend", "java-backend", "Học Java backend", "code");
    jdbc.update(
        "INSERT INTO learning_paths (id, domain_id, author_id, title, description, difficulty, "
            + "estimated_duration, is_public) VALUES (?, ?, ?, ?, ?, 'INTERMEDIATE', 120, TRUE)",
        learningPathId, domainId, userId, "Java Path", "Lộ trình Java");

    String questId = createQuestWithLearningPath(token, learningPathId);
    publishQuest(questId, token);

    String learnerEmail = "world_learner@example.com";
    String learnerToken = registerAndGetToken(learnerEmail, "world_learner_user");
    JsonNode forkBody = forkQuest(questId, learnerToken);
    String personalQuestId = forkBody.path("data").path("id").asText();
    String task1 =
        forkBody.path("data").path("chapters").get(0).path("tasks").get(0).path("id").asText();
    String task2 =
        forkBody.path("data").path("chapters").get(0).path("tasks").get(1).path("id").asText();

    ResponseEntity<String> empty =
        rest.exchange(
            "/api/v1/world/me",
            HttpMethod.GET,
            new HttpEntity<>(bearer(learnerToken)),
            String.class);
    assertThat(empty.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode emptyBody = objectMapper.readTree(empty.getBody());
    assertThat(emptyBody.path("data").path("districts")).hasSize(0);

    rest.exchange(
        "/api/v1/personal-quests/" + personalQuestId + "/tasks/" + task1 + "/complete",
        HttpMethod.PUT,
        jsonEntity(learnerToken, Map.of("evidence", Map.of())),
        String.class);
    rest.exchange(
        "/api/v1/personal-quests/" + personalQuestId + "/tasks/" + task2 + "/complete",
        HttpMethod.PUT,
        jsonEntity(learnerToken, Map.of("evidence", Map.of())),
        String.class);

    assertThat(waitForDistrictCount(learnerEmail, domainId, 2)).isTrue();

    ResponseEntity<String> response =
        rest.exchange(
            "/api/v1/world/me",
            HttpMethod.GET,
            new HttpEntity<>(bearer(learnerToken)),
            String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode body = objectMapper.readTree(response.getBody());
    JsonNode district = body.path("data").path("districts").get(0);
    assertThat(district.path("completionCount").asInt()).isEqualTo(2);
    assertThat(district.path("domainId").asText()).isEqualTo(domainId.toString());
    assertThat(district.path("domainName").asText()).isEqualTo("Java Backend");
    assertThat(district.path("domainSlug").asText()).isEqualTo("java-backend");

    rest.exchange(
        "/api/v1/personal-quests/" + personalQuestId + "/tasks/" + task2 + "/complete",
        HttpMethod.DELETE,
        new HttpEntity<>(bearer(learnerToken)),
        String.class);

    assertThat(waitForDistrictCount(learnerEmail, domainId, 1)).isTrue();

    ResponseEntity<String> afterUndo =
        rest.exchange(
            "/api/v1/world/me",
            HttpMethod.GET,
            new HttpEntity<>(bearer(learnerToken)),
            String.class);
    JsonNode afterBody = objectMapper.readTree(afterUndo.getBody());
    assertThat(afterBody.path("data").path("districts").get(0).path("completionCount").asInt())
        .isEqualTo(1);
  }

  @Test
  void world_me_otherUser_shouldReturnOwnEmptyWorld() throws Exception {
    String token = registerAndGetToken("world_other@example.com", "world_other_user");

    ResponseEntity<String> response =
        rest.exchange(
            "/api/v1/world/me",
            HttpMethod.GET,
            new HttpEntity<>(bearer(token)),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode body = objectMapper.readTree(response.getBody());
    assertThat(body.path("data").path("id").asText()).isNotBlank();
    assertThat(body.path("data").path("districts")).hasSize(0);
  }

  @Test
  void districtDetail_shouldListBuildingsAndQuests() throws Exception {
    String ownerEmail = "district_owner@example.com";
    String ownerToken = registerAndGetToken(ownerEmail, "district_owner");
    DomainFixture domain = insertDomainAndLearningPath(ownerEmail, "district_owner_lp", "Golang Backend");
    String questId = createQuestWithLearningPath(ownerToken, domain.learningPathId());
    publishQuest(questId, ownerToken);

    String learnerEmail = "district_learner@example.com";
    String learnerToken = registerAndGetToken(learnerEmail, "district_learner_user");
    JsonNode forkBody = forkQuest(questId, learnerToken);
    String personalQuestId = forkBody.path("data").path("id").asText();
    String task1 =
        forkBody.path("data").path("chapters").get(0).path("tasks").get(0).path("id").asText();
    String task2 =
        forkBody.path("data").path("chapters").get(0).path("tasks").get(1).path("id").asText();

    rest.exchange(
        "/api/v1/personal-quests/" + personalQuestId + "/tasks/" + task1 + "/complete",
        HttpMethod.PUT,
        jsonEntity(learnerToken, Map.of("evidence", Map.of())),
        String.class);
    rest.exchange(
        "/api/v1/personal-quests/" + personalQuestId + "/tasks/" + task2 + "/complete",
        HttpMethod.PUT,
        jsonEntity(learnerToken, Map.of("evidence", Map.of())),
        String.class);
    assertThat(waitForDistrictCount(learnerEmail, domain.domainId(), 2)).isTrue();

    JsonNode worldBody =
        objectMapper.readTree(
            rest.exchange(
                    "/api/v1/world/me",
                    HttpMethod.GET,
                    new HttpEntity<>(bearer(learnerToken)),
                    String.class)
                .getBody());
    String districtId = worldBody.path("data").path("districts").get(0).path("districtId").asText();

    ResponseEntity<String> response =
        rest.exchange(
            "/api/v1/world/districts/" + districtId,
            HttpMethod.GET,
            new HttpEntity<>(bearer(learnerToken)),
            String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode body = objectMapper.readTree(response.getBody());
    assertThat(body.path("data").path("completionCount").asInt()).isEqualTo(2);
    assertThat(body.path("data").path("totalTasks").asInt()).isEqualTo(2);
    assertThat(body.path("data").path("domainName").asText()).isEqualTo("Golang Backend");
    assertThat(body.path("data").path("buildings")).hasSize(1);
    assertThat(body.path("data").path("buildings").get(0).path("type").asText())
        .isEqualTo("house");
    assertThat(body.path("data").path("quests")).hasSize(1);
    assertThat(body.path("data").path("quests").get(0).path("personalQuestId").asText())
        .isEqualTo(personalQuestId);
    assertThat(body.path("data").path("quests").get(0).path("status").asText())
        .isEqualTo("COMPLETED");
  }

  @Test
  void districtDetail_manualCount_shouldLazilyUnlockBuildings() throws Exception {
    String ownerEmail = "lazy_owner@example.com";
    String ownerToken = registerAndGetToken(ownerEmail, "lazy_owner");
    DomainFixture domain = insertDomainAndLearningPath(ownerEmail, "lazy_owner_lp", "Rust Backend");
    String questId = createQuestWithLearningPath(ownerToken, domain.learningPathId());
    publishQuest(questId, ownerToken);

    String learnerEmail = "lazy_learner@example.com";
    String learnerToken = registerAndGetToken(learnerEmail, "lazy_learner_user");
    JsonNode forkBody = forkQuest(questId, learnerToken);
    String personalQuestId = forkBody.path("data").path("id").asText();
    String task1 =
        forkBody.path("data").path("chapters").get(0).path("tasks").get(0).path("id").asText();

    rest.exchange(
        "/api/v1/personal-quests/" + personalQuestId + "/tasks/" + task1 + "/complete",
        HttpMethod.PUT,
        jsonEntity(learnerToken, Map.of("evidence", Map.of())),
        String.class);
    assertThat(waitForDistrictCount(learnerEmail, domain.domainId(), 1)).isTrue();

    jdbc.update(
        "UPDATE districts SET completion_count = 5 WHERE domain_id = ? AND world_id IN "
            + "(SELECT w.id FROM worlds w JOIN users u ON u.id = w.user_id WHERE u.email = ?)",
        domain.domainId(),
        learnerEmail);

    JsonNode worldBody =
        objectMapper.readTree(
            rest.exchange(
                    "/api/v1/world/me",
                    HttpMethod.GET,
                    new HttpEntity<>(bearer(learnerToken)),
                    String.class)
                .getBody());
    String districtId = worldBody.path("data").path("districts").get(0).path("districtId").asText();

    ResponseEntity<String> response =
        rest.exchange(
            "/api/v1/world/districts/" + districtId,
            HttpMethod.GET,
            new HttpEntity<>(bearer(learnerToken)),
            String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode body = objectMapper.readTree(response.getBody());
    assertThat(body.path("data").path("completionCount").asInt()).isEqualTo(5);
    assertThat(body.path("data").path("buildings")).hasSize(2);
    assertThat(body.path("data").path("buildings").get(0).path("type").asText())
        .isEqualTo("house");
    assertThat(body.path("data").path("buildings").get(1).path("type").asText())
        .isEqualTo("school");
  }

  @Test
  void districtDetail_otherUser_shouldReturn404() throws Exception {
    String ownerEmail = "district_other_owner@example.com";
    String ownerToken = registerAndGetToken(ownerEmail, "district_other_owner");
    DomainFixture domain = insertDomainAndLearningPath(ownerEmail, "district_other_owner_lp", "Kotlin Backend");
    String questId = createQuestWithLearningPath(ownerToken, domain.learningPathId());
    publishQuest(questId, ownerToken);

    String learnerEmail = "district_other_learner@example.com";
    String learnerToken = registerAndGetToken(learnerEmail, "district_other_learner");
    JsonNode forkBody = forkQuest(questId, learnerToken);
    String personalQuestId = forkBody.path("data").path("id").asText();
    String task1 =
        forkBody.path("data").path("chapters").get(0).path("tasks").get(0).path("id").asText();
    rest.exchange(
        "/api/v1/personal-quests/" + personalQuestId + "/tasks/" + task1 + "/complete",
        HttpMethod.PUT,
        jsonEntity(learnerToken, Map.of("evidence", Map.of())),
        String.class);
    assertThat(waitForDistrictCount(learnerEmail, domain.domainId(), 1)).isTrue();

    JsonNode worldBody =
        objectMapper.readTree(
            rest.exchange(
                    "/api/v1/world/me",
                    HttpMethod.GET,
                    new HttpEntity<>(bearer(learnerToken)),
                    String.class)
                .getBody());
    String districtId = worldBody.path("data").path("districts").get(0).path("districtId").asText();

    String otherToken = registerAndGetToken("district_other@example.com", "district_other_user");
    ResponseEntity<String> response =
        rest.exchange(
            "/api/v1/world/districts/" + districtId,
            HttpMethod.GET,
            new HttpEntity<>(bearer(otherToken)),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  private boolean waitForDistrictCount(String userEmail, UUID domainId, int expected)
      throws Exception {
    for (int i = 0; i < 60; i++) {
      Integer count =
          jdbc.queryForObject(
              "SELECT COALESCE(MAX(d.completion_count), 0) FROM districts d "
                  + "JOIN worlds w ON w.id = d.world_id "
                  + "JOIN users u ON u.id = w.user_id "
                  + "WHERE u.email = ? AND d.domain_id = ?",
              Integer.class,
              userEmail,
              domainId);
      if (count != null && count == expected) {
        return true;
      }
      Thread.sleep(250);
    }
    return false;
  }

  private UUID userIdByEmail(String email) {
    return jdbc.queryForObject(
        "SELECT id FROM users WHERE email = ?", UUID.class, email);
  }

  private DomainFixture insertDomainAndLearningPath(
      String ownerEmail, String lpTitle, String domainName) {
    UUID domainId = UUID.randomUUID();
    UUID learningPathId = UUID.randomUUID();
    UUID userId = userIdByEmail(ownerEmail);
    jdbc.update(
        "INSERT INTO skill_domains (id, name, slug, description, icon) VALUES (?, ?, ?, ?, ?)",
        domainId, domainName, domainName.toLowerCase().replace(' ', '-'), "Học " + domainName, "code");
    jdbc.update(
        "INSERT INTO learning_paths (id, domain_id, author_id, title, description, difficulty, "
            + "estimated_duration, is_public) VALUES (?, ?, ?, ?, ?, 'INTERMEDIATE', 120, TRUE)",
        learningPathId, domainId, userId, lpTitle, "Lộ trình " + domainName);
    return new DomainFixture(domainId, learningPathId);
  }

  private record DomainFixture(UUID domainId, UUID learningPathId) {}

  private String createQuestWithLearningPath(String token, UUID learningPathId) {
    Map<String, Object> payload =
        Map.of(
            "title", "Spring World Quest",
            "description", "Quest trong learning path",
            "difficulty", "INTERMEDIATE",
            "learningPathId", learningPathId.toString(),
            "chapters",
                List.of(
                    Map.of(
                        "title", "Part 1",
                        "description", "Phần 1",
                        "tasks",
                            List.of(
                                Map.of(
                                    "type", "LEARN",
                                    "title", "Xem bài 1",
                                    "description", "v1",
                                    "config", Map.of()),
                                Map.of(
                                    "type", "PRACTICE",
                                    "title", "Làm bài 2",
                                    "description", "v2",
                                    "config", Map.of())))));
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

  private JsonNode forkQuest(String questId, String token) throws Exception {
    ResponseEntity<String> forkResponse =
        rest.exchange(
            "/api/v1/quests/" + questId + "/fork",
            HttpMethod.POST,
            new HttpEntity<>(bearer(token)),
            String.class);
    assertThat(forkResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    return objectMapper.readTree(forkResponse.getBody());
  }

  private void publishQuest(String questId, String token) {
    ResponseEntity<String> response =
        rest.exchange(
            "/api/v1/quests/" + questId + "/publish",
            HttpMethod.POST,
            new HttpEntity<>(bearer(token)),
            String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
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

  private HttpEntity<Map<String, Object>> jsonEntity(String token, Map<String, Object> body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(token);
    return new HttpEntity<>(body, headers);
  }

  private HttpHeaders bearer(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    return headers;
  }
}

