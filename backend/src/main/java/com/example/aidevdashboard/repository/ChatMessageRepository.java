package com.example.aidevdashboard.repository;

import com.example.aidevdashboard.model.ChatMessage;
import com.example.aidevdashboard.model.ChatRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop20ByOrderByCreatedAtDesc();

    List<ChatMessage> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);

    List<ChatMessage> findTop20ByErrorLogIdOrderByCreatedAtDesc(Long errorLogId);

    List<ChatMessage> findTop20ByErrorLogIdAndUserIdOrderByCreatedAtDesc(Long errorLogId, Long userId);

    List<ChatMessage> findTop20ByErrorLogIsNullOrderByCreatedAtDesc();

    List<ChatMessage> findTop20ByErrorLogIsNullAndUserIdOrderByCreatedAtDesc(Long userId);

    void deleteByErrorLogId(Long errorLogId);

    void deleteByErrorLogIdAndUserId(Long errorLogId, Long userId);

    void deleteByErrorLogIsNull();

    void deleteByErrorLogIsNullAndUserId(Long userId);

    void deleteByUserId(Long userId);

    long countByRole(ChatRole role);

    long countByRoleAndUserId(ChatRole role, Long userId);
}
