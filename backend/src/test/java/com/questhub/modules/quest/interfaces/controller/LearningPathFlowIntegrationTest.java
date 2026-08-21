package com.questhub.modules.quest.presentation.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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
class LearningPathFlowIntegrationTest {

  private static final UUID DOMAIN_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Container
  @ServiceConnection
  static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

  @Autowired private TestRestTemplate rest;
  @Autowired private JdbcTemplate jdbc;
  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void seedDomain() {
    jdbc.update(
        "INSERT INTO skill_domains (id, name, slug) VALUES (?, 'Programming', 'programming')"
            + " ON CONFLICT (id) DO NOTHING",
        DOMAIN_ID);
  }

  @Test
  void createPath_withValidDomain_shouldReturn201AndPersist() throws Exception {
    String token = registerAndGetToken("hieu@example.com", "hieu_nguyen");
    HttpEntity<Map<String, Object>> entity =
        jsonEntity(
            token,
            Map.of(
                "title", "Java Backend Engineer",
                "description", "Roadmap Java",
                "domainId", DOMAIN_ID.toString(),
                "difficulty", "INTERMEDIATE"));

    ResponseEntity<String> response =
        rest.exchange("/api/v1/learning-paths", HttpMethod.POST, entity, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    JsonNode body = objectMapper.readTree(response.getBody());
    assertThat(body.path("data").path("title").asText()).isEqualTo("Java Backend Engineer");
    assertThat(body.path("data").path("isPublic").asBoolean()).isFalse();
    assertThat(body.path("data").path("id").asText()).isNotBlank();

    Integer count =
        jdbc.queryForObject(
            "SELECT count(*) FROM learning_paths WHERE title = ? AND is_public = ?",
            Integer.class,
            "Java Backend Engineer",
            false);
    assertThat(count).isEqualTo(1);
  }

  @Test
  void createPath_withMissingDomain_shouldReturn404() throws Exception {
    String token = registerAndGetToken("lam@example.com", "lam_nguyen");
    HttpEntity<Map<String, Object>> entity =
        jsonEntity(
            token,
            Map.of(
                "title", "Data Science",
                "description", "",
                "domainId", UUID.randomUUID().toString(),
                "difficulty", "BEGINNER"));

    ResponseEntity<String> response =
        rest.exchange("/api/v1/learning-paths", HttpMethod.POST, entity, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void getPath_ownPrivatePath_shouldReturn200() throws Exception {
    String token = registerAndGetToken("vy@example.com", "vy_nguyen");
    String pathId = createPath(token, "Docker Mastery");

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);

    ResponseEntity<String> response =
        rest.exchange(
            "/api/v1/learning-paths/" + pathId, HttpMethod.GET, new HttpEntity<>(headers), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode body = objectMapper.readTree(response.getBody());
    assertThat(body.path("data").path("title").asText()).isEqualTo("Docker Mastery");
  }

  @Test
  void getPath_otherUsersPrivatePath_shouldReturn403() throws Exception {
    String ownerToken = registerAndGetToken("nam@example.com", "nam_nguyen");
    String pathId = createPath(ownerToken, "K8s Basics");
    String otherToken = registerAndGetToken("khoa@example.com", "khoa_nguyen");

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(otherToken);

    ResponseEntity<String> response =
        rest.exchange(
            "/api/v1/learning-paths/" + pathId, HttpMethod.GET, new HttpEntity<>(headers), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void updatePath_byOwner_shouldUpdateAndPersist() throws Exception {
    String token = registerAndGetToken("thao@example.com", "thao_nguyen");
    String pathId = createPath(token, "React Basics");

    HttpEntity<Map<String, Object>> entity =
        jsonEntity(
            token,
            Map.of(
                "title", "React Advanced",
                "description", "Nâng cao",
                "difficulty", "ADVANCED",
                "isPublic", true));

    ResponseEntity<String> response =
        rest.exchange(
            "/api/v1/learning-paths/" + pathId, HttpMethod.PUT, entity, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode body = objectMapper.readTree(response.getBody());
    assertThat(body.path("data").path("title").asText()).isEqualTo("React Advanced");
    assertThat(body.path("data").path("isPublic").asBoolean()).isTrue();

    Integer count =
        jdbc.queryForObject(
            "SELECT count(*) FROM learning_paths WHERE id = ? AND title = ? AND is_public = ?",
            Integer.class,
            UUID.fromString(pathId),
            "React Advanced",
            true);
    assertThat(count).isEqualTo(1);
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

  private String createPath(String token, String title) {
    HttpEntity<Map<String, Object>> entity =
        jsonEntity(
            token,
            Map.of(
                "title", title,
                "description", "desc",
                "domainId", DOMAIN_ID.toString(),
                "difficulty", "BEGINNER"));
    ResponseEntity<String> response =
        rest.exchange("/api/v1/learning-paths", HttpMethod.POST, entity, String.class);
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
