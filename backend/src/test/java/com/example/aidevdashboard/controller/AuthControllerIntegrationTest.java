package com.example.aidevdashboard.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.flyway.enabled=false",
                "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
                "spring.jpa.hibernate.ddl-auto=create-drop"
        })
public class AuthControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void registerAndLogin_returnToken() {
        Map<String, String> req = Map.of(
                "username", "inttestuser",
                "email", "inttest@example.com",
                "password", "Password123!"
        );

        ResponseEntity<Map> regResp = restTemplate.postForEntity("/api/auth/register", req, Map.class);
        assertThat(regResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map body = regResp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("token")).isNotNull();
        assertThat(body.get("username")).isEqualTo("inttestuser");

        ResponseEntity<Map> loginResp = restTemplate.postForEntity("/api/auth/login", req, Map.class);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map loginBody = loginResp.getBody();
        assertThat(loginBody).isNotNull();
        assertThat(loginBody.get("token")).isNotNull();
    }

    @Test
    void protectedEndpoint_requiresAuth_and_acceptsValidToken() {
        // Ensure unauthenticated requests are rejected
        ResponseEntity<String> unauth = restTemplate.getForEntity("/api/tasks", String.class);
        assertThat(unauth.getStatusCode().is4xxClientError()).isTrue();

        // Register and login to obtain token
        Map<String, String> req = Map.of(
                "username", "inttestuser2",
                "email", "inttest2@example.com",
                "password", "Password123!"
        );
        ResponseEntity<Map> regResp = restTemplate.postForEntity("/api/auth/register", req, Map.class);
        assertThat(regResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        String token = (String) regResp.getBody().get("token");
        assertThat(token).isNotBlank();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> authResp = restTemplate.exchange("/api/tasks", HttpMethod.GET, entity, String.class);
        assertThat(authResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
