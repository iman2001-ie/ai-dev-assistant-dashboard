package com.example.aidevdashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.aidevdashboard.dto.ChatRequest;
import com.example.aidevdashboard.dto.ChatResponse;
import com.example.aidevdashboard.model.ChatMessage;
import com.example.aidevdashboard.model.ChatRole;
import com.example.aidevdashboard.repository.ChatMessageRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {
    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ErrorLogService errorLogService;

    @Mock
    private AgentService agentService;

    @InjectMocks
    private ChatService chatService;

    @Test
    void sendSavesUserAndAssistantMessages() {
        when(agentService.answer("What next?", null)).thenReturn("Make a plan.");
        when(chatMessageRepository.save(org.mockito.Mockito.any(ChatMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ChatResponse response = chatService.send(new ChatRequest("What next?", null));

        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatMessageRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        List<ChatMessage> savedMessages = captor.getAllValues();
        assertThat(savedMessages.get(0).getRole()).isEqualTo(ChatRole.USER);
        assertThat(savedMessages.get(0).getContent()).isEqualTo("What next?");
        assertThat(savedMessages.get(1).getRole()).isEqualTo(ChatRole.ASSISTANT);
        assertThat(savedMessages.get(1).getContent()).isEqualTo("Make a plan.");
        assertThat(response.userMessage().role()).isEqualTo(ChatRole.USER);
        assertThat(response.assistantMessage().role()).isEqualTo(ChatRole.ASSISTANT);
    }

    @Test
    void historyLoadsNoContextMessagesWhenRequested() {
        ChatMessage message = new ChatMessage();
        message.setRole(ChatRole.USER);
        message.setContent("General question");
        when(chatMessageRepository.findTop20ByErrorLogIsNullOrderByCreatedAtDesc()).thenReturn(List.of(message));

        assertThat(chatService.history(null, true))
                .extracting("content")
                .containsExactly("General question");
        verify(chatMessageRepository, never()).findTop20ByOrderByCreatedAtDesc();
    }

    @Test
    void clearHistoryDeletesOnlySelectedContext() {
        when(errorLogService.getEntity(5L)).thenReturn(null);

        chatService.clearHistory(5L, false);

        verify(chatMessageRepository).deleteByErrorLogId(5L);
        verify(chatMessageRepository, never()).deleteAll();
    }
}
