package com.example.aidevdashboard.service;

import com.example.aidevdashboard.dto.AuthRequest;
import com.example.aidevdashboard.dto.AuthResponse;
import com.example.aidevdashboard.model.User;
import com.example.aidevdashboard.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(AuthRequest req) {
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new AuthException("Username already exists");
        }
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new AuthException("Email already exists");
        }
        User u = new User();
        u.setUsername(req.getUsername());
        u.setEmail(req.getEmail());
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        // generate refresh token
        String refresh = java.util.UUID.randomUUID().toString();
        u.setRefreshToken(refresh);
        userRepository.save(u);
        String jwt = jwtUtil.generateToken(u.getUsername());
        return new AuthResponse(jwt, u.getUsername(), refresh);
    }

    public AuthResponse login(AuthRequest req) {
        User u = userRepository.findByUsername(req.getUsername()).orElseThrow(() -> new AuthException("Invalid credentials"));
        if (!passwordEncoder.matches(req.getPassword(), u.getPasswordHash())) throw new AuthException("Invalid credentials");
        // rotate/issue refresh token
        String refresh = java.util.UUID.randomUUID().toString();
        u.setRefreshToken(refresh);
        userRepository.save(u);
        String jwt = jwtUtil.generateToken(u.getUsername());
        return new AuthResponse(jwt, u.getUsername(), refresh);
    }

    public AuthResponse refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthException("Refresh token is required");
        }
        User u = userRepository.findByRefreshToken(refreshToken).orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));
        // rotate refresh token
        String newRefresh = java.util.UUID.randomUUID().toString();
        u.setRefreshToken(newRefresh);
        userRepository.save(u);
        String jwt = jwtUtil.generateToken(u.getUsername());
        return new AuthResponse(jwt, u.getUsername(), newRefresh);
    }

    public void logout(String username) {
        userRepository.findByUsername(username).ifPresent(u -> {
            u.setRefreshToken(null);
            userRepository.save(u);
        });
    }
}
