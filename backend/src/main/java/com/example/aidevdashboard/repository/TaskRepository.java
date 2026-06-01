package com.example.aidevdashboard.repository;

import com.example.aidevdashboard.model.DeveloperTask;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TaskRepository extends JpaRepository<DeveloperTask, Long> {
    List<DeveloperTask> findTop5ByOrderByCreatedAtDesc();

    List<DeveloperTask> findAllByUserId(Long userId);

    List<DeveloperTask> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);

    java.util.Optional<DeveloperTask> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    @Modifying
    @Query("UPDATE DeveloperTask task SET task.userId = :userId WHERE task.userId IS NULL")
    int assignUnownedToUser(Long userId);
}
