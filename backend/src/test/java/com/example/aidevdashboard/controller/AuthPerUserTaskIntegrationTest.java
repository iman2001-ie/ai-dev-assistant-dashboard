package com.example.aidevdashboard.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import com.example.aidevdashboard.model.ChatMessage;
import com.example.aidevdashboard.model.ChatRole;
import com.example.aidevdashboard.model.DeveloperTask;
import com.example.aidevdashboard.model.ErrorLog;
import com.example.aidevdashboard.model.TaskPriority;
import com.example.aidevdashboard.model.TaskStatus;
import com.example.aidevdashboard.repository.ChatMessageRepository;
import com.example.aidevdashboard.repository.ErrorLogRepository;
import com.example.aidevdashboard.repository.TaskRepository;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.flyway.enabled=false",
                "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
                "spring.jpa.hibernate.ddl-auto=create-drop"
        })
public class AuthPerUserTaskIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ErrorLogRepository errorLogRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Test
    void tasks_are_scoped_to_user() {
        // register user A
        Map<String, String> reqA = Map.of(
                "username", "userA",
                "email", "userA@example.com",
                "password", "Password123!"
        );
        ResponseEntity<Map> regA = restTemplate.postForEntity("/api/auth/register", reqA, Map.class);
        assertThat(regA.getStatusCode()).isEqualTo(HttpStatus.OK);
        String tokenA = (String) regA.getBody().get("token");
        assertThat(tokenA).isNotBlank();

        // create a task as A
        HttpHeaders headersA = new HttpHeaders(); headersA.setBearerAuth(tokenA); headersA.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> taskReqA = Map.of(
                "title", "Task for A",
                "description", "Only A should see this",
                "status", "TODO",
                "priority", "MEDIUM"
        );
        ResponseEntity<Map> createA = restTemplate.postForEntity("/api/tasks", new HttpEntity<>(taskReqA, headersA), Map.class);
        assertThat(createA.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // register user B
        Map<String, String> reqB = Map.of(
                "username", "userB",
                "email", "userB@example.com",
                "password", "Password123!"
        );
        ResponseEntity<Map> regB = restTemplate.postForEntity("/api/auth/register", reqB, Map.class);
        assertThat(regB.getStatusCode()).isEqualTo(HttpStatus.OK);
        String tokenB = (String) regB.getBody().get("token");
        assertThat(tokenB).isNotBlank();

        // create a task as B
        HttpHeaders headersB = new HttpHeaders(); headersB.setBearerAuth(tokenB); headersB.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> taskReqB = Map.of(
                "title", "Task for B",
                "description", "Only B should see this",
                "status", "TODO",
                "priority", "MEDIUM"
        );
        ResponseEntity<Map> createB = restTemplate.postForEntity("/api/tasks", new HttpEntity<>(taskReqB, headersB), Map.class);
        assertThat(createB.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // A should see only A's task
        ResponseEntity<List> listA = restTemplate.exchange("/api/tasks", HttpMethod.GET, new HttpEntity<>(headersA), List.class);
        assertThat(listA.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listA.getBody()).hasSize(1);
        Map firstA = (Map) listA.getBody().get(0);
        assertThat(firstA.get("title")).isEqualTo("Task for A");

        // B should see only B's task
        ResponseEntity<List> listB = restTemplate.exchange("/api/tasks", HttpMethod.GET, new HttpEntity<>(headersB), List.class);
        assertThat(listB.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listB.getBody()).hasSize(1);
        Map firstB = (Map) listB.getBody().get(0);
        assertThat(firstB.get("title")).isEqualTo("Task for B");

        // A should not access B's single task by id
        Long bId = ((Number) firstB.get("id")).longValue();
        ResponseEntity<String> aGetsB = restTemplate.exchange("/api/tasks/" + bId, HttpMethod.GET, new HttpEntity<>(headersA), String.class);
        assertThat(aGetsB.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void logs_and_chat_history_are_scoped_to_user() {
        HttpHeaders headersA = registerHeaders("logUserA", "logUserA@example.com");
        HttpHeaders headersB = registerHeaders("logUserB", "logUserB@example.com");

        Map<String, Object> logReqA = Map.of(
                "title", "Log for A",
                "content", "Only A should see this log",
                "source", "frontend",
                "resolved", false
        );
        ResponseEntity<Map> createLogA = restTemplate.postForEntity("/api/logs", new HttpEntity<>(logReqA, headersA), Map.class);
        assertThat(createLogA.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long logAId = ((Number) createLogA.getBody().get("id")).longValue();

        Map<String, Object> chatReqA = Map.of(
                "message", "Help with A",
                "errorLogId", logAId
        );
        ResponseEntity<Map> chatA = restTemplate.postForEntity("/api/chat", new HttpEntity<>(chatReqA, headersA), Map.class);
        assertThat(chatA.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<List> logsA = restTemplate.exchange("/api/logs", HttpMethod.GET, new HttpEntity<>(headersA), List.class);
        assertThat(logsA.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(logsA.getBody()).hasSize(1);
        assertThat(((Map) logsA.getBody().get(0)).get("title")).isEqualTo("Log for A");

        ResponseEntity<List> logsB = restTemplate.exchange("/api/logs", HttpMethod.GET, new HttpEntity<>(headersB), List.class);
        assertThat(logsB.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(logsB.getBody()).isEmpty();

        ResponseEntity<String> bGetsLogA = restTemplate.exchange("/api/logs/" + logAId, HttpMethod.GET, new HttpEntity<>(headersB), String.class);
        assertThat(bGetsLogA.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ResponseEntity<List> historyA = restTemplate.exchange("/api/chat/history?errorLogId=" + logAId, HttpMethod.GET, new HttpEntity<>(headersA), List.class);
        assertThat(historyA.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(historyA.getBody()).hasSize(2);

        ResponseEntity<String> historyB = restTemplate.exchange("/api/chat/history?errorLogId=" + logAId, HttpMethod.GET, new HttpEntity<>(headersB), String.class);
        assertThat(historyB.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void authenticated_user_can_claim_unowned_seed_data() {
        DeveloperTask seedTask = new DeveloperTask();
        seedTask.setTitle("Seed task");
        seedTask.setDescription("Unowned starter task");
        seedTask.setStatus(TaskStatus.TODO);
        seedTask.setPriority(TaskPriority.MEDIUM);
        taskRepository.save(seedTask);

        ErrorLog seedLog = new ErrorLog();
        seedLog.setTitle("Seed log");
        seedLog.setContent("Unowned starter log");
        seedLog.setSource("backend");
        seedLog.setResolved(false);
        errorLogRepository.save(seedLog);

        ChatMessage seedMessage = new ChatMessage();
        seedMessage.setRole(ChatRole.ASSISTANT);
        seedMessage.setContent("Unowned starter chat");
        chatMessageRepository.save(seedMessage);

        HttpHeaders headers = registerHeaders("seedOwner", "seedOwner@example.com");

        ResponseEntity<Map> claim = restTemplate.postForEntity("/api/dev/claim-seed-data", new HttpEntity<>(null, headers), Map.class);
        assertThat(claim.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(claim.getBody().get("tasksAssigned")).isEqualTo(1);
        assertThat(claim.getBody().get("logsAssigned")).isEqualTo(1);
        assertThat(claim.getBody().get("chatMessagesAssigned")).isEqualTo(1);

        ResponseEntity<List> tasks = restTemplate.exchange("/api/tasks", HttpMethod.GET, new HttpEntity<>(headers), List.class);
        ResponseEntity<List> logs = restTemplate.exchange("/api/logs", HttpMethod.GET, new HttpEntity<>(headers), List.class);
        ResponseEntity<List> history = restTemplate.exchange("/api/chat/history?noContext=true", HttpMethod.GET, new HttpEntity<>(headers), List.class);

        assertThat(tasks.getBody()).extracting(item -> ((Map) item).get("title")).contains("Seed task");
        assertThat(logs.getBody()).extracting(item -> ((Map) item).get("title")).contains("Seed log");
        assertThat(history.getBody()).extracting(item -> ((Map) item).get("content")).contains("Unowned starter chat");
    }

    private HttpHeaders registerHeaders(String username, String email) {
        Map<String, String> request = Map.of(
                "username", username,
                "email", email,
                "password", "Password123!"
        );
        ResponseEntity<Map> response = restTemplate.postForEntity("/api/auth/register", request, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String token = (String) response.getBody().get("token");
        assertThat(token).isNotBlank();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
