package com.example.aidevdashboard.dto;

public record SeedDataClaimResponse(
        long tasksAssigned,
        long logsAssigned,
        long chatMessagesAssigned
) {
}
