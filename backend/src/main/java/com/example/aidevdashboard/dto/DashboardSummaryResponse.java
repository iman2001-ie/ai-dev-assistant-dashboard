package com.example.aidevdashboard.dto;

import java.util.List;

public record DashboardSummaryResponse(
        long taskCount,
        long unresolvedLogCount,
        long savedConversationCount,
        List<TaskResponse> recentTasks,
        List<LogResponse> recentLogs
) {
}
