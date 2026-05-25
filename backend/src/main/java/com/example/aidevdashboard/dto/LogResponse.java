package com.example.aidevdashboard.dto;

import com.example.aidevdashboard.model.ErrorLog;
import java.time.LocalDateTime;

public record LogResponse(
        Long id,
        String title,
        String content,
        String source,
        boolean resolved,
        LocalDateTime createdAt
) {
    public static LogResponse fromEntity(ErrorLog log) {
        return new LogResponse(
                log.getId(),
                log.getTitle(),
                log.getContent(),
                log.getSource(),
                log.isResolved(),
                log.getCreatedAt()
        );
    }
}
