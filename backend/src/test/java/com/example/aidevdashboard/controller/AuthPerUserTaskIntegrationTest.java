package com.example.aidevdashboard.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

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
}
