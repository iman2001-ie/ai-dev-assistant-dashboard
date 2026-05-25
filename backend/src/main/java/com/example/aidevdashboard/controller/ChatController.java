package com.example.aidevdashboard.controller;

import com.example.aidevdashboard.dto.ChatMessageResponse;
import com.example.aidevdashboard.dto.ChatRequest;
import com.example.aidevdashboard.dto.ChatResponse;
import com.example.aidevdashboard.service.ChatService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ChatResponse send(@Valid @RequestBody ChatRequest request) {
        return chatService.send(request);
    }

    @GetMapping("/history")
    public List<ChatMessageResponse> history() {
        return chatService.history();
    }
}
