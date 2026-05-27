package com.example.aidevdashboard.service;

import com.example.aidevdashboard.dto.AuthRequest;
import com.example.aidevdashboard.model.User;
import com.example.aidevdashboard.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

    public String register(AuthRequest req) {
        Optional<User> existing = userRepository.findByUsername(req.getUsername());
        if (existing.isPresent()) throw new IllegalArgumentException("Username already exists");
        User u = new User();
        u.setUsername(req.getUsername());
        u.setEmail(req.getEmail());
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        userRepository.save(u);
        return jwtUtil.generateToken(u.getUsername());
    }

    public String login(AuthRequest req) {
        User u = userRepository.findByUsername(req.getUsername()).orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordEncoder.matches(req.getPassword(), u.getPasswordHash())) throw new IllegalArgumentException("Invalid credentials");
        return jwtUtil.generateToken(u.getUsername());
    }
}
