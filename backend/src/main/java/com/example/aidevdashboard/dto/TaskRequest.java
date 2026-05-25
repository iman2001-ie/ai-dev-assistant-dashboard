package com.example.aidevdashboard.dto;

import com.example.aidevdashboard.model.TaskPriority;
import com.example.aidevdashboard.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TaskRequest(
        @NotBlank @Size(max = 160) String title,
        @Size(max = 4000) String description,
        @NotNull TaskStatus status,
        @NotNull TaskPriority priority
) {
}
