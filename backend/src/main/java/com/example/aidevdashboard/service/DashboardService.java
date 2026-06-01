package com.example.aidevdashboard.service;

import com.example.aidevdashboard.dto.DashboardSummaryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private final TaskService taskService;
    private final ErrorLogService errorLogService;
    private final ChatService chatService;

    public DashboardService(
            TaskService taskService,
            ErrorLogService errorLogService,
            ChatService chatService
    ) {
        this.taskService = taskService;
        this.errorLogService = errorLogService;
        this.chatService = chatService;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        return new DashboardSummaryResponse(
                taskService.count(),
                errorLogService.countUnresolved(),
                chatService.countAssistantMessages(),
                taskService.findRecent(),
                errorLogService.findRecent()
        );
    }
}
