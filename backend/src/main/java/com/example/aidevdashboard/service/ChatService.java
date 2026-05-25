package com.example.aidevdashboard.service;

import com.example.aidevdashboard.dto.ChatMessageResponse;
import com.example.aidevdashboard.dto.ChatRequest;
import com.example.aidevdashboard.dto.ChatResponse;
import com.example.aidevdashboard.model.ChatMessage;
import com.example.aidevdashboard.model.ChatRole;
import com.example.aidevdashboard.model.ErrorLog;
import com.example.aidevdashboard.repository.ChatMessageRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final ErrorLogService errorLogService;
    private final AgentService agentService;

    public ChatService(
            ChatMessageRepository chatMessageRepository,
            ErrorLogService errorLogService,
            AgentService agentService
    ) {
        this.chatMessageRepository = chatMessageRepository;
        this.errorLogService = errorLogService;
        this.agentService = agentService;
    }

    @Transactional
    public ChatResponse send(ChatRequest request) {
        ErrorLog selectedLog = request.errorLogId() == null ? null : errorLogService.getEntity(request.errorLogId());

        ChatMessage userMessage = new ChatMessage();
        userMessage.setRole(ChatRole.USER);
        userMessage.setContent(request.message());
        userMessage.setErrorLog(selectedLog);
        ChatMessage savedUserMessage = chatMessageRepository.save(userMessage);

        String answer = agentService.answer(request.message(), selectedLog);
        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setRole(ChatRole.ASSISTANT);
        assistantMessage.setContent(answer);
        assistantMessage.setErrorLog(selectedLog);
        ChatMessage savedAssistantMessage = chatMessageRepository.save(assistantMessage);

        return new ChatResponse(
                ChatMessageResponse.fromEntity(savedUserMessage),
                ChatMessageResponse.fromEntity(savedAssistantMessage)
        );
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> history() {
        return chatMessageRepository.findTop20ByOrderByCreatedAtDesc()
                .stream()
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt))
                .map(ChatMessageResponse::fromEntity)
                .toList();
    }
}
