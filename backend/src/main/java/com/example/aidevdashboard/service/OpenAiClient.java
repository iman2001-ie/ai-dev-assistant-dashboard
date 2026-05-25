package com.example.aidevdashboard.service;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OpenAiClient {
    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public OpenAiClient(
            RestClient.Builder restClientBuilder,
            @Value("${app.openai.api-key}") String apiKey,
            @Value("${app.openai.model}") String model
    ) {
        this.restClient = restClientBuilder.baseUrl("https://api.openai.com").build();
        this.apiKey = apiKey;
        this.model = model;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    @SuppressWarnings("unchecked")
    public String complete(String prompt) {
        if (!isConfigured()) {
            return mockResponse(prompt);
        }

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a concise senior developer assistant. Explain clearly and suggest practical next steps."),
                        Map.of("role", "user", "content", prompt)
                )
        );

        try {
            Map<String, Object> response = restClient.post()
                    .uri("/v1/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                return "The AI service returned no choices. Try again with a shorter prompt.";
            }
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return String.valueOf(message.get("content"));
        } catch (RuntimeException ex) {
            return "The OpenAI request failed, so I am returning a fallback response. Check your API key, model, and network access. Original issue: "
                    + ex.getMessage();
        }
    }

    private String mockResponse(String prompt) {
        return """
                Mock AI response:
                I do not have an OpenAI API key configured, but I can still help you reason through this.

                Suggested approach:
                1. Identify the exact error message and where it occurs.
                2. Check the most recent related task or unresolved log.
                3. Reproduce the issue with the smallest possible example.
                4. Apply one fix at a time and record what changed.

                Context received:
                %s
                """.formatted(prompt.length() > 1200 ? prompt.substring(0, 1200) + "..." : prompt);
    }
}
