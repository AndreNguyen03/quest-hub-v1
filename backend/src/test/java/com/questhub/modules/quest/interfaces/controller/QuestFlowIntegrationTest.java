package com.questhub.modules.quest.presentation.rest;

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

  @Test
  void setCompletionRule_onDraft_shouldPersistAndReturnRule() throws Exception {
    String token = registerAndGetToken("hieu@example.com", "hieu_nguyen");
    String questId = createQuest(token);

    Map<String, Object> payload =
        Map.of("completionRule", Map.of("type", "QUIZ_SCORE", "minScore", 80));

    ResponseEntity<String> response =
        rest.exchange(
            "/api/v1/quests/" + questId + "/completion-rule",
            HttpMethod.PUT,
            jsonEntity(token, payload),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode body = objectMapper.readTree(response.getBody());
    assertThat(body.path("data").path("completionRule").path("type").asText())
        .isEqualTo("QUIZ_SCORE");
    assertThat(body.path("data").path("completionRule").path("minScore").asInt()).isEqualTo(80);

    JsonNode fetched =
        objectMapper.readTree(
            rest.exchange(
                    "/api/v1/quests/" + questId,
                    HttpMethod.GET,
                    new HttpEntity<>(bearer(token)),
                    String.class)
                .getBody());
    assertThat(fetched.path("data").path("completionRule").path("type").asText())
        .isEqualTo("QUIZ_SCORE");
  }

  @Test
  void setCompletionRule_nonCreator_shouldReturn403() throws Exception {
    String ownerToken = registerAndGetToken("lien@example.com", "lien_nguyen");
    String questId = createQuest(ownerToken);
    String otherToken = registerAndGetToken("nam@example.com", "nam_nguyen");

    ResponseEntity<String> response =
        rest.exchange(
            "/api/v1/quests/" + questId + "/completion-rule",
            HttpMethod.PUT,
            jsonEntity(otherToken, Map.of("completionRule", Map.of("type", "ALL_TASKS"))),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void setCompletionRule_publicQuest_shouldReturn409() throws Exception {
    String token = registerAndGetToken("hoa@example.com", "hoa_nguyen");
    String questId = createQuest(token);

    ResponseEntity<String> publishResponse =
        rest.exchange(
            "/api/v1/quests/" + questId + "/publish",
            HttpMethod.POST,
            new HttpEntity<>(bearer(token)),
            String.class);
    assertThat(publishResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    ResponseEntity<String> response =
        rest.exchange(
            "/api/v1/quests/" + questId + "/completion-rule",
            HttpMethod.PUT,
            jsonEntity(token, Map.of("completionRule", Map.of("type", "ALL_TASKS"))),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void forkQuest_publicQuest_shouldCopyTreeAndPersistPersonalQuest() throws Exception {
    String ownerToken = registerAndGetToken("lan@example.com", "lan_nguyen");
    String questId = createQuest(ownerToken);
    publishQuest(questId, ownerToken);

    String learnerToken = registerAndGetToken("minh@example.com", "minh_nguyen");
    ResponseEntity<String> response =
        rest.exchange(
            "/api/v1/quests/" + questId + "/fork",
            HttpMethod.POST,
            new HttpEntity<>(bearer(learnerToken)),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    JsonNode body = objectMapper.readTree(response.getBody());
    assertThat(body.path("data").path("questId").asText()).isEqualTo(questId);
    assertThat(body.path("data").path("status").asText()).isEqualTo("ACTIVE");
    assertThat(body.path("data").path("progress").asInt()).isEqualTo(0);
    assertThat(body.path("data").path("chapters")).hasSize(1);
    assertThat(body.path("data").path("chapters").get(0).path("tasks")).hasSize(2);

    String personalQuestId = body.path("data").path("id").asText();
    Integer personalQuests =
        jdbc.queryForObject(
            "SELECT count(*) FROM personal_quests WHERE id = ? AND user_id IS NOT NULL",
            Integer.class,
            UUID.fromString(personalQuestId));
    assertThat(personalQuests).isEqualTo(1);
    Integer personalChapters =
        jdbc.queryForObject(
            "SELECT count(*) FROM personal_chapters WHERE personal_quest_id = ?",
            Integer.class,
            UUID.fromString(personalQuestId));
    assertThat(personalChapters).isEqualTo(1);
    Integer personalTasks =
        jdbc.queryForObject(
            "SELECT count(*) FROM personal_tasks WHERE personal_chapter_id IN "
                + "(SELECT id FROM personal_chapters WHERE personal_quest_id = ?)",
            Integer.class,
            UUID.fromString(personalQuestId));
    assertThat(personalTasks).isEqualTo(2);

    Integer originalQuests =
        jdbc.queryForObject(
            "SELECT count(*) FROM quests WHERE id = ?", Integer.class, UUID.fromString(questId));
    assertThat(originalQuests).isEqualTo(1);
    Integer originalChapters =
        jdbc.queryForObject(
            "SELECT count(*) FROM chapters WHERE quest_id = ?", Integer.class, UUID.fromString(questId));
    assertThat(originalChapters).isEqualTo(1);
    Integer originalTasks =
        jdbc.queryForObject(
            "SELECT count(*) FROM tasks WHERE chapter_id IN "
                + "(SELECT id FROM chapters WHERE quest_id = ?)",
            Integer.class,
            UUID.fromString(questId));
    assertThat(originalTasks).isEqualTo(2);

    assertThat(waitForForkCount(questId, 1)).isTrue();

    ResponseEntity<String> listResponse =
        rest.exchange(
            "/api/v1/personal-quests",
            HttpMethod.GET,
            new HttpEntity<>(bearer(learnerToken)),
            String.class);
    assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode listBody = objectMapper.readTree(listResponse.getBody());
    assertThat(listBody.path("data")).hasSize(1);
    assertThat(listBody.path("data").get(0).path("id").asText()).isEqualTo(personalQuestId);
  }

  @Test
  void forkQuest_draftQuest_shouldReturn403() throws Exception {
    String ownerToken = registerAndGetToken("ngoc@example.com", "ngoc_nguyen");
    String questId = createQuest(ownerToken);
    String learnerToken = registerAndGetToken("thanh@example.com", "thanh_nguyen");

    ResponseEntity<String> response =
        rest.exchange(
            "/api/v1/quests/" + questId + "/fork",
            HttpMethod.POST,
            new HttpEntity<>(bearer(learnerToken)),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void forkQuest_secondFork_shouldReturn409() throws Exception {
    String ownerToken = registerAndGetToken("tuan@example.com", "tuan_nguyen");
    String questId = createQuest(ownerToken);
    publishQuest(questId, ownerToken);

    String learnerToken = registerAndGetToken("khanh@example.com", "khanh_nguyen");
    ResponseEntity<String> first =
        rest.exchange(
            "/api/v1/quests/" + questId + "/fork",
            HttpMethod.POST,
            new HttpEntity<>(bearer(learnerToken)),
            String.class);
    assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    ResponseEntity<String> second =
        rest.exchange(
            "/api/v1/quests/" + questId + "/fork",
            HttpMethod.POST,
            new HttpEntity<>(bearer(learnerToken)),
            String.class);

    assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void getPersonalQuest_owner_shouldReturn200_otherUserShouldReturn404() throws Exception {
    String ownerToken = registerAndGetToken("quang@example.com", "quang_nguyen");
    String questId = createQuest(ownerToken);
    publishQuest(questId, ownerToken);

    String learnerToken = registerAndGetToken("huong@example.com", "huong_nguyen");
    ResponseEntity<String> forkResponse =
        rest.exchange(
            "/api/v1/quests/" + questId + "/fork",
            HttpMethod.POST,
            new HttpEntity<>(bearer(learnerToken)),
            String.class);
    assertThat(forkResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    String personalQuestId =
        objectMapper.readTree(forkResponse.getBody()).path("data").path("id").asText();

    ResponseEntity<String> ownerView =
        rest.exchange(
            "/api/v1/personal-quests/" + personalQuestId,
            HttpMethod.GET,
            new HttpEntity<>(bearer(learnerToken)),
            String.class);
    assertThat(ownerView.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode ownerBody = objectMapper.readTree(ownerView.getBody());
    assertThat(ownerBody.path("data").path("title").asText())
        .isEqualTo("Spring Security Fundamentals");

    String otherToken = registerAndGetToken("viet@example.com", "viet_nguyen");
    ResponseEntity<String> otherView =
        rest.exchange(
            "/api/v1/personal-quests/" + personalQuestId,
            HttpMethod.GET,
            new HttpEntity<>(bearer(otherToken)),
            String.class);
    assertThat(otherView.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void completeAndUndoTask_shouldPersistCompletionAndRevert() throws Exception {
    String ownerToken = registerAndGetToken("thao@example.com", "thao_nguyen");
    String questId = createQuest(ownerToken);
    publishQuest(questId, ownerToken);

    String learnerToken = registerAndGetToken("my@example.com", "my_nguyen");
    JsonNode forkBody = forkQuest(questId, learnerToken);
    String personalQuestId = forkBody.path("data").path("id").asText();
    String taskId =
        forkBody.path("data").path("chapters").get(0).path("tasks").get(0).path("id").asText();

    ResponseEntity<String> completeResponse =
        rest.exchange(
            "/api/v1/personal-quests/" + personalQuestId + "/tasks/" + taskId + "/complete",
            HttpMethod.PUT,
            jsonEntity(learnerToken, Map.of("evidence", Map.of())),
            String.class);
    assertThat(completeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode completeBody = objectMapper.readTree(completeResponse.getBody());
    assertThat(completeBody.path("data").path("progress").asInt()).isEqualTo(50);
    assertThat(
            completeBody.path("data").path("chapters").get(0).path("tasks").get(0).path("isCompleted").asBoolean())
        .isTrue();

    Integer completions =
        jdbc.queryForObject(
            "SELECT count(*) FROM task_completions WHERE personal_task_id = ?",
            Integer.class,
            UUID.fromString(taskId));
    assertThat(completions).isEqualTo(1);

    ResponseEntity<String> doubleComplete =
        rest.exchange(
            "/api/v1/personal-quests/" + personalQuestId + "/tasks/" + taskId + "/complete",
            HttpMethod.PUT,
            jsonEntity(learnerToken, Map.of("evidence", Map.of())),
            String.class);
    assertThat(doubleComplete.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

    ResponseEntity<String> undoResponse =
        rest.exchange(
            "/api/v1/personal-quests/" + personalQuestId + "/tasks/" + taskId + "/complete",
            HttpMethod.DELETE,
            new HttpEntity<>(bearer(learnerToken)),
            String.class);
    assertThat(undoResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode undoBody = objectMapper.readTree(undoResponse.getBody());
    assertThat(undoBody.path("data").path("progress").asInt()).isEqualTo(0);
    assertThat(
            undoBody.path("data").path("chapters").get(0).path("tasks").get(0).path("isCompleted").asBoolean())
        .isFalse();

    Integer remaining =
        jdbc.queryForObject(
            "SELECT count(*) FROM task_completions WHERE personal_task_id = ?",
            Integer.class,
            UUID.fromString(taskId));
    assertThat(remaining).isZero();
  }

  @Test
  void completeTask_submissionWithoutEvidence_shouldReturn400() throws Exception {
    String ownerToken = registerAndGetToken("tam@example.com", "tam_nguyen");
    String questId = createSubmissionQuest(ownerToken);
    publishQuest(questId, ownerToken);

    String learnerToken = registerAndGetToken("hanh@example.com", "hanh_nguyen");
    JsonNode forkBody = forkQuest(questId, learnerToken);
    String personalQuestId = forkBody.path("data").path("id").asText();
    String submissionTaskId = null;
    for (JsonNode chapter : forkBody.path("data").path("chapters")) {
      for (JsonNode task : chapter.path("tasks")) {
        if ("SUBMISSION".equals(task.path("type").asText())) {
          submissionTaskId = task.path("id").asText();
        }
      }
    }
    assertThat(submissionTaskId).isNotNull();

    ResponseEntity<String> response =
        rest.exchange(
            "/api/v1/personal-quests/" + personalQuestId + "/tasks/" + submissionTaskId + "/complete",
            HttpMethod.PUT,
            jsonEntity(learnerToken, Map.of("evidence", Map.of())),
            String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    ResponseEntity<String> withEvidence =
        rest.exchange(
            "/api/v1/personal-quests/" + personalQuestId + "/tasks/" + submissionTaskId + "/complete",
            HttpMethod.PUT,
            jsonEntity(learnerToken, Map.of("evidence", Map.of("url", "https://example.com/nop-bai"))),
            String.class);
    assertThat(withEvidence.getStatusCode()).isEqualTo(HttpStatus.OK);
    Integer completions =
        jdbc.queryForObject(
            "SELECT count(*) FROM task_completions WHERE personal_task_id = ?",
            Integer.class,
            UUID.fromString(submissionTaskId));
    assertThat(completions).isEqualTo(1);
  }

  @Test
  void completeTask_otherUser_shouldReturn404() throws Exception {
    String ownerToken = registerAndGetToken("son@example.com", "son_nguyen");
    String questId = createQuest(ownerToken);
    publishQuest(questId, ownerToken);

    String learnerToken = registerAndGetToken("trang@example.com", "trang_nguyen");
    JsonNode forkBody = forkQuest(questId, learnerToken);
    String personalQuestId = forkBody.path("data").path("id").asText();
    String taskId =
        forkBody.path("data").path("chapters").get(0).path("tasks").get(0).path("id").asText();

    String otherToken = registerAndGetToken("kiet@example.com", "kiet_nguyen");
    ResponseEntity<String> response =
        rest.exchange(
            "/api/v1/personal-quests/" + personalQuestId + "/tasks/" + taskId + "/complete",
            HttpMethod.PUT,
            jsonEntity(otherToken, Map.of("evidence", Map.of())),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void submitQuiz_pass_shouldCompleteTaskAndPersistRecords() throws Exception {
    String ownerToken = registerAndGetToken("diem@example.com", "diem_nguyen");
    String questId = createQuizQuest(ownerToken);
    publishQuest(questId, ownerToken);

    String learnerToken = registerAndGetToken("ngan@example.com", "ngan_nguyen");
    JsonNode forkBody = forkQuest(questId, learnerToken);
    String personalQuestId = forkBody.path("data").path("id").asText();
    String quizTaskId = quizTaskId(forkBody);

    ResponseEntity<String> response =
        rest.exchange(
            "/api/v1/personal-quests/" + personalQuestId + "/tasks/" + quizTaskId + "/quiz-attempts",
            HttpMethod.POST,
            jsonEntity(learnerToken, Map.of("answers", Map.of("q1", "a", "q2", "b"))),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    JsonNode body = objectMapper.readTree(response.getBody());
    assertThat(body.path("data").path("passed").asBoolean()).isTrue();
    assertThat(body.path("data").path("taskCompleted").asBoolean()).isTrue();
    assertThat(body.path("data").path("attemptId").asText()).isNotBlank();

    Integer attempts =
        jdbc.queryForObject(
            "SELECT count(*) FROM quiz_attempts WHERE personal_task_id = ?",
            Integer.class,
            UUID.fromString(quizTaskId));
    assertThat(attempts).isEqualTo(1);
    Integer completedTasks =
        jdbc.queryForObject(
            "SELECT count(*) FROM personal_tasks WHERE id = ? AND is_completed = TRUE",
            Integer.class,
            UUID.fromString(quizTaskId));
    assertThat(completedTasks).isEqualTo(1);
    Integer completions =
        jdbc.queryForObject(
            "SELECT count(*) FROM task_completions WHERE personal_task_id = ?",
            Integer.class,
            UUID.fromString(quizTaskId));
    assertThat(completions).isEqualTo(1);
  }

  @Test
  void submitQuiz_failThenPass_shouldShowBothInHistory() throws Exception {
    String ownerToken = registerAndGetToken("bao@example.com", "bao_nguyen");
    String questId = createQuizQuest(ownerToken);
    publishQuest(questId, ownerToken);

    String learnerToken = registerAndGetToken("uyen@example.com", "uyen_nguyen");
    JsonNode forkBody = forkQuest(questId, learnerToken);
    String personalQuestId = forkBody.path("data").path("id").asText();
    String quizTaskId = quizTaskId(forkBody);

    ResponseEntity<String> failed =
        rest.exchange(
            "/api/v1/personal-quests/" + personalQuestId + "/tasks/" + quizTaskId + "/quiz-attempts",
            HttpMethod.POST,
            jsonEntity(learnerToken, Map.of("answers", Map.of("q1", "b", "q2", "a"))),
            String.class);
    assertThat(failed.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    JsonNode failedBody = objectMapper.readTree(failed.getBody());
    assertThat(failedBody.path("data").path("passed").asBoolean()).isFalse();
    assertThat(failedBody.path("data").path("taskCompleted").asBoolean()).isFalse();

    ResponseEntity<String> passed =
        rest.exchange(
            "/api/v1/personal-quests/" + personalQuestId + "/tasks/" + quizTaskId + "/quiz-attempts",
            HttpMethod.POST,
            jsonEntity(learnerToken, Map.of("answers", Map.of("q1", "a", "q2", "b"))),
            String.class);
    assertThat(passed.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    JsonNode passedBody = objectMapper.readTree(passed.getBody());
    assertThat(passedBody.path("data").path("passed").asBoolean()).isTrue();

    ResponseEntity<String> history =
        rest.exchange(
            "/api/v1/personal-quests/" + personalQuestId + "/tasks/" + quizTaskId + "/quiz-attempts",
            HttpMethod.GET,
            new HttpEntity<>(bearer(learnerToken)),
            String.class);
    assertThat(history.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode historyBody = objectMapper.readTree(history.getBody());
    assertThat(historyBody.path("data")).hasSize(2);
    assertThat(historyBody.path("data").get(0).path("passed").asBoolean()).isTrue();
    assertThat(historyBody.path("data").get(1).path("passed").asBoolean()).isFalse();

    Integer completions =
        jdbc.queryForObject(
            "SELECT count(*) FROM task_completions WHERE personal_task_id = ?",
            Integer.class,
            UUID.fromString(quizTaskId));
    assertThat(completions).isEqualTo(1);
  }

  @Test
  void submitQuiz_otherUser_shouldReturn404() throws Exception {
    String ownerToken = registerAndGetToken("loc@example.com", "loc_nguyen");
    String questId = createQuizQuest(ownerToken);
    publishQuest(questId, ownerToken);

    String learnerToken = registerAndGetToken("nhi@example.com", "nhi_nguyen");
    JsonNode forkBody = forkQuest(questId, learnerToken);
    String personalQuestId = forkBody.path("data").path("id").asText();
    String quizTaskId = quizTaskId(forkBody);

    String otherToken = registerAndGetToken("phuc@example.com", "phuc_nguyen");
    ResponseEntity<String> response =
        rest.exchange(
            "/api/v1/personal-quests/" + personalQuestId + "/tasks/" + quizTaskId + "/quiz-attempts",
            HttpMethod.POST,
            jsonEntity(otherToken, Map.of("answers", Map.of("q1", "a", "q2", "b"))),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void editPersonalQuest_addTask_shouldNotAffectOriginalOrOtherFork() throws Exception {
    String ownerToken = registerAndGetToken("giang@example.com", "giang_nguyen");
    String questId = createQuest(ownerToken);
    publishQuest(questId, ownerToken);

    String learnerAToken = registerAndGetToken("truc@example.com", "truc_nguyen");
    JsonNode forkA = forkQuest(questId, learnerAToken);
    String personalQuestA = forkA.path("data").path("id").asText();
    String chapterA =
        forkA.path("data").path("chapters").get(0).path("id").asText();

    String learnerBToken = registerAndGetToken("cam@example.com", "cam_nguyen");
    JsonNode forkB = forkQuest(questId, learnerBToken);
    String personalQuestB = forkB.path("data").path("id").asText();

    ResponseEntity<String> addTask =
        rest.exchange(
            "/api/v1/personal-quests/" + personalQuestA + "/chapters/" + chapterA + "/tasks",
            HttpMethod.POST,
            jsonEntity(
                learnerAToken,
                Map.of("type", "PRACTICE", "title", "Task riêng", "config", Map.of())),
            String.class);
    assertThat(addTask.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    JsonNode addBody = objectMapper.readTree(addTask.getBody());
    assertThat(addBody.path("data").path("chapters").get(0).path("tasks")).hasSize(3);
    JsonNode added = addBody.path("data").path("chapters").get(0).path("tasks").get(2);
    assertThat(added.path("isCompleted").asBoolean()).isFalse();
    assertThat(added.path("sourceTaskId").isNull()).isTrue();

    Integer originalTasks =
        jdbc.queryForObject(
            "SELECT count(*) FROM tasks WHERE chapter_id IN "
                + "(SELECT id FROM chapters WHERE quest_id = ?)",
            Integer.class,
            UUID.fromString(questId));
    assertThat(originalTasks).isEqualTo(2);
    Integer otherForkTasks =
        jdbc.queryForObject(
            "SELECT count(*) FROM personal_tasks WHERE personal_chapter_id IN "
                + "(SELECT id FROM personal_chapters WHERE personal_quest_id = ?)",
            Integer.class,
            UUID.fromString(personalQuestB));
    assertThat(otherForkTasks).isEqualTo(2);

    String addedTaskId = added.path("id").asText();
    ResponseEntity<String> deleteTask =
        rest.exchange(
            "/api/v1/personal-quests/" + personalQuestA + "/chapters/" + chapterA + "/tasks/" + addedTaskId,
            HttpMethod.DELETE,
            new HttpEntity<>(bearer(learnerAToken)),
            String.class);
    assertThat(deleteTask.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode deleteBody = objectMapper.readTree(deleteTask.getBody());
    assertThat(deleteBody.path("data").path("chapters").get(0).path("tasks")).hasSize(2);

    Integer remaining =
        jdbc.queryForObject(
            "SELECT count(*) FROM personal_tasks WHERE id = ?",
            Integer.class,
            UUID.fromString(addedTaskId));
    assertThat(remaining).isZero();
  }

  @Test
  void editPersonalQuest_completedQuest_shouldReturn409() throws Exception {
    String ownerToken = registerAndGetToken("an@example.com", "an_nguyen");
    String questId = createQuest(ownerToken);
    publishQuest(questId, ownerToken);

    String learnerToken = registerAndGetToken("tuyet@example.com", "tuyet_nguyen");
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

    ResponseEntity<String> addChapter =
        rest.exchange(
            "/api/v1/personal-quests/" + personalQuestId + "/chapters",
            HttpMethod.POST,
            jsonEntity(learnerToken, Map.of("title", "Chapter mới")),
            String.class);
    assertThat(addChapter.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

    ResponseEntity<String> deleteTask =
        rest.exchange(
            "/api/v1/personal-quests/" + personalQuestId + "/chapters/"
                + forkBody.path("data").path("chapters").get(0).path("id").asText()
                + "/tasks/" + task1,
            HttpMethod.DELETE,
            new HttpEntity<>(bearer(learnerToken)),
            String.class);
    assertThat(deleteTask.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void editPersonalQuest_otherUser_shouldReturn404() throws Exception {
    String ownerToken = registerAndGetToken("dai@example.com", "dai_nguyen");
    String questId = createQuest(ownerToken);
    publishQuest(questId, ownerToken);

    String learnerToken = registerAndGetToken("xuan@example.com", "xuan_nguyen");
    JsonNode forkBody = forkQuest(questId, learnerToken);
    String personalQuestId = forkBody.path("data").path("id").asText();

    String otherToken = registerAndGetToken("hung@example.com", "hung_nguyen");
    ResponseEntity<String> response =
        rest.exchange(
            "/api/v1/personal-quests/" + personalQuestId + "/chapters",
            HttpMethod.POST,
            jsonEntity(otherToken, Map.of("title", "Không của tôi")),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void editPersonalQuest_reorderChapters_shouldPersistPositions() throws Exception {
    String ownerToken = registerAndGetToken("phat@example.com", "phat_nguyen");
    Map<String, Object> payload =
        Map.of(
            "title", "Web Security",
            "description", "Học bảo mật web",
            "difficulty", "INTERMEDIATE",
            "chapters",
                List.of(
                    Map.of("title", "Auth", "description", "phần 1", "tasks",
                        List.of(Map.of("type", "LEARN", "title", "auth video", "config", Map.of()))),
                    Map.of("title", "Crypto", "description", "phần 2", "tasks",
                        List.of(Map.of("type", "LEARN", "title", "crypto video", "config", Map.of())))));
    ResponseEntity<String> create =
        rest.exchange("/api/v1/quests", HttpMethod.POST, jsonEntity(ownerToken, payload), String.class);
    assertThat(create.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    String questId = objectMapper.readTree(create.getBody()).path("data").path("id").asText();
    publishQuest(questId, ownerToken);

    String learnerToken = registerAndGetToken("yen@example.com", "yen_nguyen");
    JsonNode forkBody = forkQuest(questId, learnerToken);
    String personalQuestId = forkBody.path("data").path("id").asText();
    String firstChapter =
        forkBody.path("data").path("chapters").get(0).path("id").asText();
    String secondChapter =
        forkBody.path("data").path("chapters").get(1).path("id").asText();

    ResponseEntity<String> response =
        rest.exchange(
            "/api/v1/personal-quests/" + personalQuestId + "/chapters/reorder",
            HttpMethod.PATCH,
            jsonEntity(learnerToken, Map.of("chapterIds", List.of(secondChapter, firstChapter))),
            String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode body = objectMapper.readTree(response.getBody());
    assertThat(body.path("data").path("chapters").get(0).path("id").asText())
        .isEqualTo(secondChapter);
    assertThat(body.path("data").path("chapters").get(0).path("position").asInt()).isEqualTo(0);
    assertThat(body.path("data").path("chapters").get(1).path("id").asText())
        .isEqualTo(firstChapter);
    assertThat(body.path("data").path("chapters").get(1).path("position").asInt()).isEqualTo(1);

    Integer positions =
        jdbc.queryForObject(
            "SELECT count(*) FROM personal_chapters WHERE personal_quest_id = ? AND id = ? AND position = 0",
            Integer.class,
            UUID.fromString(personalQuestId),
            UUID.fromString(secondChapter));
    assertThat(positions).isEqualTo(1);
  }

  private String quizTaskId(JsonNode forkBody) {
    for (JsonNode chapter : forkBody.path("data").path("chapters")) {
      for (JsonNode task : chapter.path("tasks")) {
        if ("QUIZ".equals(task.path("type").asText())) {
          return task.path("id").asText();
        }
      }
    }
    return null;
  }

  private String createQuizQuest(String token) {
    Map<String, Object> payload =
        Map.of(
            "title", "TypeScript Fundamentals",
            "description", "Học TypeScript",
            "difficulty", "INTERMEDIATE",
            "chapters",
                List.of(
                    Map.of(
                        "title", "Type Basics",
                        "description", "Phần 1",
                        "tasks",
                            List.of(
                                Map.of(
                                    "type", "QUIZ",
                                    "title", "Quiz basics",
                                    "description", "trắc nghiệm",
                                    "config",
                                        Map.of(
                                            "passThreshold", 80,
                                            "questions",
                                                List.of(
                                                    Map.of("id", "q1", "options", List.of("a", "b"), "correctAnswer", "a"),
                                                    Map.of("id", "q2", "options", List.of("a", "b"), "correctAnswer", "b"))))))));
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

  private String createSubmissionQuest(String token) {
    Map<String, Object> payload =
        Map.of(
            "title", "Portfolio Bootcamp",
            "description", "Xây dựng portfolio",
            "difficulty", "INTERMEDIATE",
            "chapters",
                List.of(
                    Map.of(
                        "title", "Portfolio",
                        "description", "Phần 1",
                        "tasks",
                            List.of(
                                Map.of(
                                    "type", "SUBMISSION",
                                    "title", "Nộp portfolio",
                                    "description", "link GitHub",
                                    "config", Map.of()),
                                Map.of(
                                    "type", "REFLECTION",
                                    "title", "Reflect",
                                    "description", "review",
                                    "config", Map.of("minLength", 5))))));
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

  @Test
  void completeAllTasks_shouldCompleteQuestAndPublishEvent() throws Exception {
    String ownerToken = registerAndGetToken("khoa@example.com", "khoa_nguyen");
    String questId = createQuest(ownerToken);
    publishQuest(questId, ownerToken);

    String learnerToken = registerAndGetToken("nguyen@example.com", "nguyen_nguyen");
    JsonNode forkBody = forkQuest(questId, learnerToken);
    String personalQuestId = forkBody.path("data").path("id").asText();
    String task1 =
        forkBody.path("data").path("chapters").get(0).path("tasks").get(0).path("id").asText();
    String task2 =
        forkBody.path("data").path("chapters").get(0).path("tasks").get(1).path("id").asText();

    ResponseEntity<String> first =
        rest.exchange(
            "/api/v1/personal-quests/" + personalQuestId + "/tasks/" + task1 + "/complete",
            HttpMethod.PUT,
            jsonEntity(learnerToken, Map.of("evidence", Map.of())),
            String.class);
    assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode firstBody = objectMapper.readTree(first.getBody());
    assertThat(firstBody.path("data").path("progress").asInt()).isEqualTo(50);
    assertThat(firstBody.path("data").path("status").asText()).isEqualTo("ACTIVE");

    ResponseEntity<String> second =
        rest.exchange(
            "/api/v1/personal-quests/" + personalQuestId + "/tasks/" + task2 + "/complete",
            HttpMethod.PUT,
            jsonEntity(learnerToken, Map.of("evidence", Map.of())),
            String.class);
    assertThat(second.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode secondBody = objectMapper.readTree(second.getBody());
    assertThat(secondBody.path("data").path("progress").asInt()).isEqualTo(100);
    assertThat(secondBody.path("data").path("status").asText()).isEqualTo("COMPLETED");
    assertThat(secondBody.path("data").path("completedAt").asText()).isNotBlank();

    Integer completed =
        jdbc.queryForObject(
            "SELECT count(*) FROM personal_quests WHERE id = ? AND status = 'COMPLETED' AND completed_at IS NOT NULL",
            Integer.class,
            UUID.fromString(personalQuestId));
    assertThat(completed).isEqualTo(1);

    Integer events =
        jdbc.queryForObject(
            "SELECT count(*) FROM outbox_events WHERE event_type = 'quest.completed' AND payload::text LIKE ?",
            Integer.class,
            "%" + personalQuestId + "%");
    assertThat(events).isGreaterThanOrEqualTo(1);
  }

  @Test
  void undoAfterCompletion_shouldReopenQuestToActive() throws Exception {
    String ownerToken = registerAndGetToken("linh@example.com", "linh_nguyen");
    String questId = createQuest(ownerToken);
    publishQuest(questId, ownerToken);

    String learnerToken = registerAndGetToken("vy@example.com", "vy_nguyen");
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

    ResponseEntity<String> undoResponse =
        rest.exchange(
            "/api/v1/personal-quests/" + personalQuestId + "/tasks/" + task2 + "/complete",
            HttpMethod.DELETE,
            new HttpEntity<>(bearer(learnerToken)),
            String.class);
    assertThat(undoResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode undoBody = objectMapper.readTree(undoResponse.getBody());
    assertThat(undoBody.path("data").path("progress").asInt()).isEqualTo(50);
    assertThat(undoBody.path("data").path("status").asText()).isEqualTo("ACTIVE");
    assertThat(undoBody.path("data").path("completedAt").isNull()).isTrue();

    Integer active =
        jdbc.queryForObject(
            "SELECT count(*) FROM personal_quests WHERE id = ? AND status = 'ACTIVE' AND completed_at IS NULL",
            Integer.class,
            UUID.fromString(personalQuestId));
    assertThat(active).isEqualTo(1);
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

  private boolean waitForForkCount(String questId, int expected) throws Exception {
    for (int i = 0; i < 40; i++) {
      Integer count =
          jdbc.queryForObject(
              "SELECT fork_count FROM quests WHERE id = ?", Integer.class, UUID.fromString(questId));
      if (count != null && count == expected) {
        return true;
      }
      Thread.sleep(250);
    }
    return false;
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

  private HttpHeaders bearer(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    return headers;
  }
}
