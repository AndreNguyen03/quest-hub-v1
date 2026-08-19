package com.questhub.modules.identity.interfaces.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
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
class RegisterFlowIntegrationTest {

  private static final String REGISTER_URL = "/api/v1/auth/register";

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
  void register_valid_shouldReturn201WithTokensAndPersistUser() throws Exception {
    ResponseEntity<String> response =
        rest.postForEntity(
            REGISTER_URL,
            Map.of(
                "email", "minh@example.com",
                "username", "minh_nguyen",
                "displayName", "Minh Nguyen",
                "password", "matkhau123"),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    JsonNode body = objectMapper.readTree(response.getBody());
    assertThat(body.path("data").path("accessToken").asText()).isNotBlank();
    assertThat(body.path("data").path("refreshToken").asText()).isNotBlank();

    Integer count =
        jdbc.queryForObject(
            "SELECT count(*) FROM users WHERE username = ? AND email = ?",
            Integer.class,
            "minh_nguyen",
            "minh@example.com");
    assertThat(count).isEqualTo(1);

    String hash =
        jdbc.queryForObject(
            "SELECT password_hash FROM users WHERE username = ?", String.class, "minh_nguyen");
    assertThat(hash).startsWith("$2a$");
  }

  @Test
  void login_validCredentials_shouldReturnTokens() throws Exception {
    registerUser("lan@example.com", "lan_nguyen");

    ResponseEntity<String> response =
        rest.postForEntity(
            "/api/v1/auth/login",
            Map.of("email", "lan@example.com", "password", "matkhau123"),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode body = objectMapper.readTree(response.getBody());
    assertThat(body.path("data").path("accessToken").asText()).isNotBlank();
    assertThat(body.path("data").path("refreshToken").asText()).isNotBlank();
  }

  @Test
  void login_wrongPassword_shouldReturn401InvalidCredentials() throws Exception {
    registerUser("mai@example.com", "mai_nguyen");

    ResponseEntity<String> response =
        rest.postForEntity(
            "/api/v1/auth/login",
            Map.of("email", "mai@example.com", "password", "sai-mat-khau"),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    JsonNode body = objectMapper.readTree(response.getBody());
    assertThat(body.path("error").path("code").asText())
        .isEqualTo("INVALID_CREDENTIALS");
  }

  @Test
  void refresh_withValidRefreshToken_shouldRotateAndIssueNewPair() throws Exception {
    JsonNode tokens = registerUser("phuc@example.com", "phuc_nguyen");
    String refresh = tokens.path("data").path("refreshToken").asText();

    ResponseEntity<String> response =
        rest.postForEntity(
            "/api/v1/auth/refresh", Map.of("refreshToken", refresh), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode body = objectMapper.readTree(response.getBody());
    assertThat(body.path("data").path("accessToken").asText()).isNotBlank();
    assertThat(body.path("data").path("refreshToken").asText()).isNotBlank();

    ResponseEntity<String> reuse =
        rest.postForEntity(
            "/api/v1/auth/refresh", Map.of("refreshToken", refresh), String.class);
    assertThat(reuse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void updateProfile_withAccessToken_shouldUpdateAndPersist() throws Exception {
    JsonNode tokens = registerUser("tuan@example.com", "tuan_nguyen");
    String access = tokens.path("data").path("accessToken").asText();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(access);
    HttpEntity<Map<String, Object>> entity =
        new HttpEntity<>(
            Map.of(
                "avatarUrl", "http://x.com/tuan.png",
                "bio", "Java dev",
                "displayName", "Tuan Nguyen",
                "isPublic", false),
            headers);

    ResponseEntity<String> response =
        rest.exchange("/api/v1/users/me", HttpMethod.PUT, entity, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode body = objectMapper.readTree(response.getBody());
    assertThat(body.path("data").path("displayName").asText()).isEqualTo("Tuan Nguyen");
    assertThat(body.path("data").path("bio").asText()).isEqualTo("Java dev");
    assertThat(body.path("data").path("isPublic").asBoolean()).isFalse();

    Integer count =
        jdbc.queryForObject(
            "SELECT count(*) FROM users WHERE username = ? AND avatar_url = ? AND bio = ? AND is_public = ?",
            Integer.class,
            "tuan_nguyen",
            "http://x.com/tuan.png",
            "Java dev",
            false);
    assertThat(count).isEqualTo(1);
  }

  @Test
  void protectedEndpoint_withoutToken_shouldReturn401() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> entity =
        new HttpEntity<>(Map.of("displayName", "X", "isPublic", true), headers);

    ResponseEntity<String> response =
        rest.exchange("/api/v1/users/me", HttpMethod.PUT, entity, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void getMe_withAccessToken_shouldReturnCurrentUserProfile() throws Exception {
    JsonNode tokens = registerUser("hong@example.com", "hong_nguyen");
    String access = tokens.path("data").path("accessToken").asText();

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(access);

    ResponseEntity<String> response =
        rest.exchange("/api/v1/users/me", HttpMethod.GET, new HttpEntity<>(headers), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode body = objectMapper.readTree(response.getBody());
    assertThat(body.path("data").path("username").asText()).isEqualTo("hong_nguyen");
    assertThat(body.path("data").path("email").asText()).isEqualTo("hong@example.com");
    assertThat(body.path("data").path("displayName").asText()).isEqualTo("Test User");
    assertThat(body.path("data").path("id").asText()).isNotBlank();
  }

  @Test
  void getMe_withoutToken_shouldReturn401() {
    ResponseEntity<String> response =
        rest.exchange("/api/v1/users/me", HttpMethod.GET, null, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void register_duplicateEmailAndUsername_shouldReturn409WithBothDetails() throws Exception {
    Map<String, String> payload =
        Map.of(
            "email", "duy@example.com",
            "username", "duy_nguyen",
            "displayName", "Duy Nguyen",
            "password", "matkhau123");
    rest.postForEntity(REGISTER_URL, payload, String.class);

    ResponseEntity<String> response = rest.postForEntity(REGISTER_URL, payload, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    JsonNode body = objectMapper.readTree(response.getBody());
    assertThat(body.path("error").path("code").asText()).isEqualTo("CONFLICT");
    assertThat(body.path("error").path("details").size()).isEqualTo(2);
    assertThat(body.path("error").path("details").findValuesAsText("field"))
        .containsExactlyInAnyOrder("email", "username");
  }

  @Test
  void register_invalidPayload_shouldReturn400ValidationError() throws Exception {
    ResponseEntity<String> response =
        rest.postForEntity(
            REGISTER_URL,
            Map.of(
                "email", "khong-phai-email",
                "username", "ABC!",
                "displayName", "",
                "password", "123"),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    JsonNode body = objectMapper.readTree(response.getBody());
    assertThat(body.path("error").path("code").asText()).isEqualTo("VALIDATION_ERROR");
  }

  private JsonNode registerUser(String email, String username) throws Exception {
    ResponseEntity<String> response =
        rest.postForEntity(
            REGISTER_URL,
            Map.of(
                "email", email,
                "username", username,
                "displayName", "Test User",
                "password", "matkhau123"),
            String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    return objectMapper.readTree(response.getBody());
  }
}