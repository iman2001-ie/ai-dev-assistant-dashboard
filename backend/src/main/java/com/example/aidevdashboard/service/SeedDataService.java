package com.example.aidevdashboard.service;

import com.example.aidevdashboard.dto.SeedDataClaimResponse;
import com.example.aidevdashboard.repository.ChatMessageRepository;
import com.example.aidevdashboard.repository.ErrorLogRepository;
import com.example.aidevdashboard.repository.TaskRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeedDataService {
    private final TaskRepository taskRepository;
    private final ErrorLogRepository errorLogRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final CurrentUserService currentUserService;

    public SeedDataService(
            TaskRepository taskRepository,
            ErrorLogRepository errorLogRepository,
            ChatMessageRepository chatMessageRepository,
            CurrentUserService currentUserService
    ) {
        this.taskRepository = taskRepository;
        this.errorLogRepository = errorLogRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public SeedDataClaimResponse claimUnownedSeedData() {
        Long userId = currentUserService.currentUserId();
        if (userId == null) {
            throw new AccessDeniedException("Login is required to claim seed data");
        }

        int tasksAssigned = taskRepository.assignUnownedToUser(userId);
        int logsAssigned = errorLogRepository.assignUnownedToUser(userId);
        int chatMessagesAssigned = chatMessageRepository.assignUnownedToUser(userId);

        return new SeedDataClaimResponse(tasksAssigned, logsAssigned, chatMessagesAssigned);
    }
}
