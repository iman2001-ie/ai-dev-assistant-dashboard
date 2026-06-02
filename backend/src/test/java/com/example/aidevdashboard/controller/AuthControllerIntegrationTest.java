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
        assertThat(body.get("refreshToken")).isNotNull();

        ResponseEntity<Map> loginResp = restTemplate.postForEntity("/api/auth/login", req, Map.class);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map loginBody = loginResp.getBody();
        assertThat(loginBody).isNotNull();
        assertThat(loginBody.get("token")).isNotNull();
        assertThat(loginBody.get("username")).isEqualTo("inttestuser");
        assertThat(loginBody.get("refreshToken")).isNotNull();
    }

    @Test
    void registerDuplicateUsername_returnsClearClientError() {
        Map<String, String> req = Map.of(
                "username", "duplicateuser",
                "email", "duplicate@example.com",
                "password", "Password123!"
        );

        ResponseEntity<Map> first = restTemplate.postForEntity("/api/auth/register", req, Map.class);
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Map> duplicate = restTemplate.postForEntity("/api/auth/register", req, Map.class);

        assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(duplicate.getBody()).isNotNull();
        assertThat(duplicate.getBody().get("message")).isEqualTo("Username already exists");
        assertThat(duplicate.getBody()).doesNotContainKey("token");
        assertThat(duplicate.getBody()).doesNotContainKey("refreshToken");
    }

    @Test
    void registerDuplicateEmail_returnsClearClientError() {
        Map<String, String> firstReq = Map.of(
                "username", "emailuser1",
                "email", "shared-email@example.com",
                "password", "Password123!"
        );
        ResponseEntity<Map> first = restTemplate.postForEntity("/api/auth/register", firstReq, Map.class);
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map<String, String> duplicateEmailReq = Map.of(
                "username", "emailuser2",
                "email", "shared-email@example.com",
                "password", "Password123!"
        );
        ResponseEntity<Map> duplicate = restTemplate.postForEntity("/api/auth/register", duplicateEmailReq, Map.class);

        assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(duplicate.getBody()).isNotNull();
        assertThat(duplicate.getBody().get("message")).isEqualTo("Email already exists");
        assertThat(duplicate.getBody()).doesNotContainKey("token");
        assertThat(duplicate.getBody()).doesNotContainKey("refreshToken");
    }

    @Test
    void loginMissingAccount_returnsClearClientErrorWithoutTokens() {
        ResponseEntity<Map> missingLogin = restTemplate.postForEntity("/api/auth/login", Map.of(
                "username", "missingloginuser",
                "password", "Password123!"
        ), Map.class);

        assertThat(missingLogin.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(missingLogin.getBody()).isNotNull();
        assertThat(missingLogin.getBody().get("message")).isEqualTo("The account does not exist");
        assertThat(missingLogin.getBody()).doesNotContainKey("token");
        assertThat(missingLogin.getBody()).doesNotContainKey("refreshToken");
    }

    @Test
    void loginWrongPassword_returnsClearClientErrorWithoutTokens() {
        Map<String, String> req = Map.of(
                "username", "invalidloginuser",
                "email", "invalidlogin@example.com",
                "password", "Password123!"
        );
        ResponseEntity<Map> register = restTemplate.postForEntity("/api/auth/register", req, Map.class);
        assertThat(register.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Map> invalidLogin = restTemplate.postForEntity("/api/auth/login", Map.of(
                "username", "invalidloginuser",
                "password", "wrong-password"
        ), Map.class);

        assertThat(invalidLogin.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(invalidLogin.getBody()).isNotNull();
        assertThat(invalidLogin.getBody().get("message")).isEqualTo("Wrong password");
        assertThat(invalidLogin.getBody()).doesNotContainKey("token");
        assertThat(invalidLogin.getBody()).doesNotContainKey("refreshToken");
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

    @Test
    void publicAuthRoutes_remainAccessibleWithoutBearerToken() {
        Map<String, String> req = Map.of(
                "username", "publicauthuser",
                "email", "publicauth@example.com",
                "password", "Password123!"
        );

        ResponseEntity<Map> register = restTemplate.postForEntity("/api/auth/register", req, Map.class);
        assertThat(register.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Map> login = restTemplate.postForEntity("/api/auth/login", req, Map.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(login.getBody()).isNotNull();

        String refreshToken = (String) login.getBody().get("refreshToken");
        ResponseEntity<Map> refresh = restTemplate.postForEntity("/api/auth/refresh", Map.of("refreshToken", refreshToken), Map.class);
        assertThat(refresh.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void profileCanBeReadAndUpdated() {
        Map<String, String> req = Map.of(
                "username", "profileuser",
                "email", "profile@example.com",
                "password", "Password123!"
        );
        ResponseEntity<Map> register = restTemplate.postForEntity("/api/auth/register", req, Map.class);
        assertThat(register.getStatusCode()).isEqualTo(HttpStatus.OK);
        String token = (String) register.getBody().get("token");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> profile = restTemplate.exchange("/api/auth/me", HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        assertThat(profile.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(profile.getBody().get("username")).isEqualTo("profileuser");
        assertThat(profile.getBody().get("email")).isEqualTo("profile@example.com");

        Map<String, String> updateReq = Map.of(
                "username", "profileuser2",
                "email", "profile2@example.com",
                "currentPassword", "Password123!",
                "newPassword", "Password456!"
        );
        ResponseEntity<Map> update = restTemplate.exchange("/api/auth/me", HttpMethod.PUT, new HttpEntity<>(updateReq, headers), Map.class);
        assertThat(update.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(update.getBody().get("username")).isEqualTo("profileuser2");
        assertThat(update.getBody().get("email")).isEqualTo("profile2@example.com");
        assertThat(update.getBody().get("token")).isNotNull();
        assertThat(update.getBody().get("refreshToken")).isNotNull();

        ResponseEntity<Map> oldLogin = restTemplate.postForEntity("/api/auth/login", Map.of(
                "username", "profileuser",
                "password", "Password123!"
        ), Map.class);
        assertThat(oldLogin.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ResponseEntity<Map> newLogin = restTemplate.postForEntity("/api/auth/login", Map.of(
                "username", "profileuser2",
                "password", "Password456!"
        ), Map.class);
        assertThat(newLogin.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void profileUpdateRejectsDuplicateUsernameAndEmail() {
        ResponseEntity<Map> first = restTemplate.postForEntity("/api/auth/register", Map.of(
                "username", "profileduplicate1",
                "email", "profileduplicate1@example.com",
                "password", "Password123!"
        ), Map.class);
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Map> second = restTemplate.postForEntity("/api/auth/register", Map.of(
                "username", "profileduplicate2",
                "email", "profileduplicate2@example.com",
                "password", "Password123!"
        ), Map.class);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.OK);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth((String) second.getBody().get("token"));
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> duplicateUsername = restTemplate.exchange("/api/auth/me", HttpMethod.PUT, new HttpEntity<>(Map.of(
                "username", "profileduplicate1",
                "email", "profileduplicate2@example.com"
        ), headers), Map.class);
        assertThat(duplicateUsername.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(duplicateUsername.getBody().get("message")).isEqualTo("Username already exists");

        ResponseEntity<Map> duplicateEmail = restTemplate.exchange("/api/auth/me", HttpMethod.PUT, new HttpEntity<>(Map.of(
                "username", "profileduplicate2",
                "email", "profileduplicate1@example.com"
        ), headers), Map.class);
        assertThat(duplicateEmail.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(duplicateEmail.getBody().get("message")).isEqualTo("Email already exists");
    }

    @Test
    void accountCanBeDeleted() {
        Map<String, String> req = Map.of(
                "username", "deleteaccountuser",
                "email", "deleteaccount@example.com",
                "password", "Password123!"
        );
        ResponseEntity<Map> register = restTemplate.postForEntity("/api/auth/register", req, Map.class);
        assertThat(register.getStatusCode()).isEqualTo(HttpStatus.OK);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth((String) register.getBody().get("token"));

        ResponseEntity<Void> deleted = restTemplate.exchange("/api/auth/me", HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Map> login = restTemplate.postForEntity("/api/auth/login", Map.of(
                "username", "deleteaccountuser",
                "password", "Password123!"
        ), Map.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(login.getBody().get("message")).isEqualTo("The account does not exist");
    }

    @Test
    void protectedEndpoint_rejectsInvalidBearerToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("not-a-valid-jwt");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> resp = restTemplate.exchange("/api/tasks", HttpMethod.GET, entity, String.class);

        assertThat(resp.getStatusCode().is4xxClientError()).isTrue();
    }

}
