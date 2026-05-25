package com.example.aidevdashboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LogRequest(
        @NotBlank @Size(max = 180) String title,
        @NotBlank @Size(max = 12000) String content,
        @Size(max = 120) String source,
        boolean resolved
) {
}
