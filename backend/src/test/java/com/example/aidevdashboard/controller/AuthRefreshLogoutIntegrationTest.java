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
public class AuthRefreshLogoutIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void refresh_and_logout_flow() {
        Map<String, String> req = Map.of(
                "username", "refreshuser",
                "email", "refresh@example.com",
                "password", "Password123!"
        );
        ResponseEntity<Map> reg = restTemplate.postForEntity("/api/auth/register", req, Map.class);
        assertThat(reg.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map body = reg.getBody();
        String refreshToken = (String) body.get("refreshToken");
        String jwt = (String) body.get("token");
        assertThat(refreshToken).isNotBlank();
        assertThat(jwt).isNotBlank();

        // Refresh using refresh token
        ResponseEntity<Map> refreshed = restTemplate.postForEntity("/api/auth/refresh", Map.of("refreshToken", refreshToken), Map.class);
        assertThat(refreshed.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map refreshedBody = refreshed.getBody();
        assertThat(refreshedBody.get("token")).isNotNull();
        String newRefresh = (String) refreshedBody.get("refreshToken");
        assertThat(newRefresh).isNotEqualTo(refreshToken);

        // Logout using authenticated jwt
        HttpHeaders headers = new HttpHeaders(); headers.setBearerAuth((String) refreshedBody.get("token"));
        ResponseEntity<Void> logoutResp = restTemplate.postForEntity("/api/auth/logout", new HttpEntity<>(headers), Void.class);
        assertThat(logoutResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // using the old refresh token should fail
        ResponseEntity<Map> failedRefresh = restTemplate.postForEntity("/api/auth/refresh", Map.of("refreshToken", newRefresh), Map.class);
        // after logout, refresh token should be invalidated -> 404
        assertThat(failedRefresh.getStatusCode().is4xxClientError()).isTrue();
    }
}
