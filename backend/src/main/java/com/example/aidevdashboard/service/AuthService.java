package com.example.aidevdashboard.service;

import com.example.aidevdashboard.dto.AuthRequest;
import com.example.aidevdashboard.dto.AuthResponse;
import com.example.aidevdashboard.dto.ProfileUpdateRequest;
import com.example.aidevdashboard.dto.UserProfileResponse;
import com.example.aidevdashboard.model.User;
import com.example.aidevdashboard.repository.ChatMessageRepository;
import com.example.aidevdashboard.repository.ErrorLogRepository;
import com.example.aidevdashboard.repository.TaskRepository;
import com.example.aidevdashboard.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CurrentUserService currentUserService;
    private final TaskRepository taskRepository;
    private final ErrorLogRepository errorLogRepository;
    private final ChatMessageRepository chatMessageRepository;

    public AuthService(
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            CurrentUserService currentUserService,
            TaskRepository taskRepository,
            ErrorLogRepository errorLogRepository,
            ChatMessageRepository chatMessageRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.currentUserService = currentUserService;
        this.taskRepository = taskRepository;
        this.errorLogRepository = errorLogRepository;
        this.chatMessageRepository = chatMessageRepository;
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
        return new AuthResponse(jwt, u.getUsername(), refresh, u.getEmail());
    }

    public AuthResponse login(AuthRequest req) {
        User u = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new AuthException("The account does not exist"));
        if (!passwordEncoder.matches(req.getPassword(), u.getPasswordHash())) {
            throw new AuthException("Wrong password");
        }
        // rotate/issue refresh token
        String refresh = java.util.UUID.randomUUID().toString();
        u.setRefreshToken(refresh);
        userRepository.save(u);
        String jwt = jwtUtil.generateToken(u.getUsername());
        return new AuthResponse(jwt, u.getUsername(), refresh, u.getEmail());
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
        return new AuthResponse(jwt, u.getUsername(), newRefresh, u.getEmail());
    }

    public void logout(String username) {
        userRepository.findByUsername(username).ifPresent(u -> {
            u.setRefreshToken(null);
            userRepository.save(u);
        });
    }

    public UserProfileResponse currentProfile() {
        User user = currentUserService.currentUser()
                .orElseThrow(() -> new AuthException("Login is required"));
        return new UserProfileResponse(user.getUsername(), user.getEmail());
    }

    public AuthResponse updateProfile(ProfileUpdateRequest req) {
        User user = currentUserService.currentUser()
                .orElseThrow(() -> new AuthException("Login is required"));

        String username = normalize(req.getUsername());
        String email = normalize(req.getEmail());
        if (username == null) {
            throw new AuthException("Username is required");
        }
        if (email == null || !email.contains("@")) {
            throw new AuthException("Valid email is required");
        }

        userRepository.findByUsername(username)
                .filter(existing -> !existing.getId().equals(user.getId()))
                .ifPresent(existing -> {
                    throw new AuthException("Username already exists");
                });
        userRepository.findByEmail(email)
                .filter(existing -> !existing.getId().equals(user.getId()))
                .ifPresent(existing -> {
                    throw new AuthException("Email already exists");
                });

        String newPassword = normalize(req.getNewPassword());
        if (newPassword != null) {
            String currentPassword = normalize(req.getCurrentPassword());
            if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
                throw new AuthException("Current password is incorrect");
            }
            if (newPassword.length() < 6) {
                throw new AuthException("New password must be at least 6 characters");
            }
            user.setPasswordHash(passwordEncoder.encode(newPassword));
        }

        user.setUsername(username);
        user.setEmail(email);
        String refresh = java.util.UUID.randomUUID().toString();
        user.setRefreshToken(refresh);
        userRepository.save(user);

        String jwt = jwtUtil.generateToken(user.getUsername());
        return new AuthResponse(jwt, user.getUsername(), refresh, user.getEmail());
    }

    @Transactional
    public void deleteCurrentAccount() {
        User user = currentUserService.currentUser()
                .orElseThrow(() -> new AuthException("Login is required"));

        Long userId = user.getId();
        chatMessageRepository.deleteByUserId(userId);
        taskRepository.deleteByUserId(userId);
        errorLogRepository.deleteByUserId(userId);
        userRepository.delete(user);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
