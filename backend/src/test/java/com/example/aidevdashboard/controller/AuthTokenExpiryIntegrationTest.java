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
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "app.jwt.expiration-seconds=1"
        })
public class AuthTokenExpiryIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void expiredToken_rejected() throws InterruptedException {
        Map<String, String> req = Map.of(
                "username", "expiryuser",
                "email", "expiry@example.com",
                "password", "Password123!"
        );
        ResponseEntity<Map> reg = restTemplate.postForEntity("/api/auth/register", req, Map.class);
        assertThat(reg.getStatusCode()).isEqualTo(HttpStatus.OK);
        String token = (String) reg.getBody().get("token");
        assertThat(token).isNotBlank();

        // wait for token to expire (expiration-seconds=1)
        Thread.sleep(1200);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> resp = restTemplate.exchange("/api/tasks", HttpMethod.GET, entity, String.class);
        assertThat(resp.getStatusCode().is4xxClientError()).isTrue();
    }
}
