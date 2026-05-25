package com.example.aidevdashboard.dto;

import com.example.aidevdashboard.model.ChatMessage;
import com.example.aidevdashboard.model.ChatRole;
import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long id,
        ChatRole role,
        String content,
        Long errorLogId,
        LocalDateTime createdAt
) {
    public static ChatMessageResponse fromEntity(ChatMessage message) {
        Long logId = message.getErrorLog() == null ? null : message.getErrorLog().getId();
        return new ChatMessageResponse(
                message.getId(),
                message.getRole(),
                message.getContent(),
                logId,
                message.getCreatedAt()
        );
    }
}
