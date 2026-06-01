package com.example.aidevdashboard.service;

import com.example.aidevdashboard.model.User;
import com.example.aidevdashboard.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Long currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        String username = auth.getName();
        if (username == null) {
            return null;
        }

        return userRepository.findByUsername(username).map(User::getId).orElse(null);
    }
}
