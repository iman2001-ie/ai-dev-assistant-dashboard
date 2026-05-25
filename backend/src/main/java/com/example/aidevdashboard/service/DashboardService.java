package com.example.aidevdashboard.service;

import com.example.aidevdashboard.dto.DashboardSummaryResponse;
import com.example.aidevdashboard.model.ChatRole;
import com.example.aidevdashboard.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private final TaskService taskService;
    private final ErrorLogService errorLogService;
    private final ChatMessageRepository chatMessageRepository;

    public DashboardService(
            TaskService taskService,
            ErrorLogService errorLogService,
            ChatMessageRepository chatMessageRepository
    ) {
        this.taskService = taskService;
        this.errorLogService = errorLogService;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        return new DashboardSummaryResponse(
                taskService.count(),
                errorLogService.countUnresolved(),
                chatMessageRepository.countByRole(ChatRole.ASSISTANT),
                taskService.findRecent(),
                errorLogService.findRecent()
        );
    }
}
