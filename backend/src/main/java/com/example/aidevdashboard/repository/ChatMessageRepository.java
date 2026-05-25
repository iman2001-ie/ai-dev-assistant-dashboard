package com.example.aidevdashboard.repository;

import com.example.aidevdashboard.model.ChatMessage;
import com.example.aidevdashboard.model.ChatRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop20ByOrderByCreatedAtDesc();

    List<ChatMessage> findTop20ByErrorLogIdOrderByCreatedAtDesc(Long errorLogId);

    List<ChatMessage> findTop20ByErrorLogIsNullOrderByCreatedAtDesc();

    void deleteByErrorLogId(Long errorLogId);

    void deleteByErrorLogIsNull();

    long countByRole(ChatRole role);
}
