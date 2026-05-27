package com.example.aidevdashboard.controller;

import com.example.aidevdashboard.dto.AuthRequest;
import com.example.aidevdashboard.dto.AuthResponse;
import com.example.aidevdashboard.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest req) {
        String token = authService.register(req);
        return ResponseEntity.ok(new AuthResponse(token, req.getUsername()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest req) {
        String token = authService.login(req);
        return ResponseEntity.ok(new AuthResponse(token, req.getUsername()));
    }
}
