package com.example.aidevdashboard.dto;

public record ChatResponse(
        ChatMessageResponse userMessage,
        ChatMessageResponse assistantMessage
) {
}
