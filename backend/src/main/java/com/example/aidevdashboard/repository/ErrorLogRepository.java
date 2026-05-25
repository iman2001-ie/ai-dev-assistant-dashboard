package com.example.aidevdashboard.repository;

import com.example.aidevdashboard.model.ErrorLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {
    List<ErrorLog> findByResolvedFalseOrderByCreatedAtDesc();

    List<ErrorLog> findTop5ByOrderByCreatedAtDesc();

    long countByResolvedFalse();
}
