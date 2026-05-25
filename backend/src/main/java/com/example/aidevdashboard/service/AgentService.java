package com.example.aidevdashboard.service;

import com.example.aidevdashboard.dto.LogResponse;
import com.example.aidevdashboard.dto.TaskResponse;
import com.example.aidevdashboard.model.ErrorLog;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgentService {
    private final TaskService taskService;
    private final ErrorLogService errorLogService;
    private final OpenAiClient openAiClient;

    public AgentService(TaskService taskService, ErrorLogService errorLogService, OpenAiClient openAiClient) {
        this.taskService = taskService;
        this.errorLogService = errorLogService;
        this.openAiClient = openAiClient;
    }

    @Transactional(readOnly = true)
    public String answer(String userMessage, ErrorLog selectedLog) {
        String lower = userMessage.toLowerCase(Locale.ROOT);
        StringBuilder prompt = new StringBuilder();
        prompt.append("User question:\n").append(userMessage).append("\n\n");

        if (selectedLog != null) {
            prompt.append("Selected error log:\n")
                    .append(summarizeLog(selectedLog))
                    .append("\n\n");
        }

        if (lower.contains("task") || lower.contains("todo") || lower.contains("work")) {
            prompt.append("Recent tasks from getRecentTasks:\n");
            List<TaskResponse> tasks = taskService.findRecent();
            tasks.forEach(task -> prompt.append("- ")
                    .append(task.title())
                    .append(" [").append(task.status()).append(", ").append(task.priority()).append("]\n"));
            prompt.append("\n");
        }

        if (lower.contains("log") || lower.contains("error") || lower.contains("stack") || lower.contains("bug")) {
            prompt.append("Unresolved logs from getUnresolvedLogs:\n");
            List<LogResponse> logs = errorLogService.findUnresolved();
            logs.forEach(log -> prompt.append("- ")
                    .append(log.title())
                    .append(" from ").append(log.source() == null ? "unknown" : log.source())
                    .append("\n"));
            prompt.append("\n");
        }

        prompt.append("Respond with a clear diagnosis, likely next steps, and any assumptions.");
        return openAiClient.complete(prompt.toString());
    }

    public String summarizeLog(ErrorLog log) {
        String content = log.getContent();
        String trimmed = content.length() > 1600 ? content.substring(0, 1600) + "..." : content;
        return """
                Title: %s
                Source: %s
                Resolved: %s
                Content:
                %s
                """.formatted(log.getTitle(), log.getSource(), log.isResolved(), trimmed);
    }
}
