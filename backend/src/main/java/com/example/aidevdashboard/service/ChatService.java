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
    private final CurrentUserService currentUserService;

    public ChatService(
            ChatMessageRepository chatMessageRepository,
            ErrorLogService errorLogService,
            AgentService agentService,
            CurrentUserService currentUserService
    ) {
        this.chatMessageRepository = chatMessageRepository;
        this.errorLogService = errorLogService;
        this.agentService = agentService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public ChatResponse send(ChatRequest request) {
        Long userId = currentUserService.currentUserId();
        ErrorLog selectedLog = request.errorLogId() == null ? null : errorLogService.getEntity(request.errorLogId());

        ChatMessage userMessage = new ChatMessage();
        userMessage.setRole(ChatRole.USER);
        userMessage.setContent(request.message());
        userMessage.setErrorLog(selectedLog);
        userMessage.setUserId(userId);
        ChatMessage savedUserMessage = chatMessageRepository.save(userMessage);

        String answer = agentService.answer(request.message(), selectedLog);
        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setRole(ChatRole.ASSISTANT);
        assistantMessage.setContent(answer);
        assistantMessage.setErrorLog(selectedLog);
        assistantMessage.setUserId(userId);
        ChatMessage savedAssistantMessage = chatMessageRepository.save(assistantMessage);

        return new ChatResponse(
                ChatMessageResponse.fromEntity(savedUserMessage),
                ChatMessageResponse.fromEntity(savedAssistantMessage)
        );
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> history(Long errorLogId, boolean noContext) {
        Long userId = currentUserService.currentUserId();
        List<ChatMessage> messages;
        if (errorLogId != null) {
            errorLogService.getEntity(errorLogId);
            messages = userId == null
                    ? chatMessageRepository.findTop20ByErrorLogIdOrderByCreatedAtDesc(errorLogId)
                    : chatMessageRepository.findTop20ByErrorLogIdAndUserIdOrderByCreatedAtDesc(errorLogId, userId);
        } else if (noContext) {
            messages = userId == null
                    ? chatMessageRepository.findTop20ByErrorLogIsNullOrderByCreatedAtDesc()
                    : chatMessageRepository.findTop20ByErrorLogIsNullAndUserIdOrderByCreatedAtDesc(userId);
        } else {
            messages = userId == null
                    ? chatMessageRepository.findTop20ByOrderByCreatedAtDesc()
                    : chatMessageRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId);
        }

        return messages
                .stream()
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt))
                .map(ChatMessageResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void clearHistory(Long errorLogId, boolean noContext) {
        Long userId = currentUserService.currentUserId();
        if (errorLogId != null) {
            errorLogService.getEntity(errorLogId);
            if (userId == null) {
                chatMessageRepository.deleteByErrorLogId(errorLogId);
            } else {
                chatMessageRepository.deleteByErrorLogIdAndUserId(errorLogId, userId);
            }
        } else if (noContext) {
            if (userId == null) {
                chatMessageRepository.deleteByErrorLogIsNull();
            } else {
                chatMessageRepository.deleteByErrorLogIsNullAndUserId(userId);
            }
        } else {
            if (userId == null) {
                chatMessageRepository.deleteAll();
            } else {
                chatMessageRepository.deleteByUserId(userId);
            }
        }
    }

    @Transactional(readOnly = true)
    public long countAssistantMessages() {
        Long userId = currentUserService.currentUserId();
        if (userId != null) {
            return chatMessageRepository.countByRoleAndUserId(ChatRole.ASSISTANT, userId);
        }
        return chatMessageRepository.countByRole(ChatRole.ASSISTANT);
    }
}
