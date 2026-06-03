package com.example.aidevdashboard.repository;

import com.example.aidevdashboard.model.ErrorLog;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {
    List<ErrorLog> findAllByOrderByResolvedAscCreatedAtDesc();

    List<ErrorLog> findAllByUserIdOrderByResolvedAscCreatedAtDesc(Long userId);

    List<ErrorLog> findByResolvedFalseOrderByCreatedAtDesc();

    List<ErrorLog> findByResolvedFalseAndUserIdOrderByCreatedAtDesc(Long userId);

    List<ErrorLog> findByOrderByResolvedAscCreatedAtDesc(Pageable pageable);

    List<ErrorLog> findByUserIdOrderByResolvedAscCreatedAtDesc(Long userId, Pageable pageable);

    long countByResolvedFalse();

    long countByResolvedFalseAndUserId(Long userId);

    Optional<ErrorLog> findByIdAndUserId(Long id, Long userId);

    @Modifying
    @Query("UPDATE ErrorLog log SET log.userId = :userId WHERE log.userId IS NULL")
    int assignUnownedToUser(Long userId);

    void deleteByUserId(Long userId);
}
