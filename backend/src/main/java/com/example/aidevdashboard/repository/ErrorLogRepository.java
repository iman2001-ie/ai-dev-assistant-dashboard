package com.example.aidevdashboard.repository;

import com.example.aidevdashboard.model.ErrorLog;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {
    List<ErrorLog> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    List<ErrorLog> findByResolvedFalseOrderByCreatedAtDesc();

    List<ErrorLog> findByResolvedFalseAndUserIdOrderByCreatedAtDesc(Long userId);

    List<ErrorLog> findTop5ByOrderByCreatedAtDesc();

    List<ErrorLog> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);

    long countByResolvedFalse();

    long countByResolvedFalseAndUserId(Long userId);

    Optional<ErrorLog> findByIdAndUserId(Long id, Long userId);
}
