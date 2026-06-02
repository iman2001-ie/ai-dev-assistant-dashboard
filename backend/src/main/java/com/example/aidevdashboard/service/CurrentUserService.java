package com.example.aidevdashboard.service;

import com.example.aidevdashboard.model.User;
import com.example.aidevdashboard.repository.UserRepository;
import java.util.Optional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Long currentUserId() {
        return currentUser().map(User::getId).orElse(null);
    }

    public Optional<User> currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }

        String username = auth.getName();
        if (username == null) {
            return Optional.empty();
        }

        return userRepository.findByUsername(username);
    }
}
