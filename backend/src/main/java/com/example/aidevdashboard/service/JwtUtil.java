package com.example.aidevdashboard.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret:change-me}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-seconds:3600}")
    private long expirationSeconds;

    public String generateToken(String username) {
        Algorithm alg = Algorithm.HMAC256(jwtSecret);
        return JWT.create()
                .withSubject(username)
                .withIssuedAt(Date.from(Instant.now()))
                .withExpiresAt(Date.from(Instant.now().plusSeconds(expirationSeconds)))
                .sign(alg);
    }

    public String getSubject(String token) {
        return JWT.require(Algorithm.HMAC256(jwtSecret)).build().verify(token).getSubject();
    }
}
