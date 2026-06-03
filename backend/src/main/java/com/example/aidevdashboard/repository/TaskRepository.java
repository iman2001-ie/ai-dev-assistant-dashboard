package com.example.aidevdashboard.repository;

import com.example.aidevdashboard.model.DeveloperTask;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TaskRepository extends JpaRepository<DeveloperTask, Long> {
    @Query("""
            SELECT task FROM DeveloperTask task
            ORDER BY
                CASE
                    WHEN task.priority = com.example.aidevdashboard.model.TaskPriority.HIGH THEN 0
                    WHEN task.priority = com.example.aidevdashboard.model.TaskPriority.MEDIUM THEN 1
                    ELSE 2
                END,
                task.createdAt DESC
            """)
    List<DeveloperTask> findAllOrderedByPriority();

    @Query("""
            SELECT task FROM DeveloperTask task
            WHERE task.userId = :userId
            ORDER BY
                CASE
                    WHEN task.priority = com.example.aidevdashboard.model.TaskPriority.HIGH THEN 0
                    WHEN task.priority = com.example.aidevdashboard.model.TaskPriority.MEDIUM THEN 1
                    ELSE 2
                END,
                task.createdAt DESC
            """)
    List<DeveloperTask> findAllByUserIdOrderedByPriority(Long userId);

    @Query("""
            SELECT task FROM DeveloperTask task
            ORDER BY
                CASE
                    WHEN task.priority = com.example.aidevdashboard.model.TaskPriority.HIGH THEN 0
                    WHEN task.priority = com.example.aidevdashboard.model.TaskPriority.MEDIUM THEN 1
                    ELSE 2
                END,
                task.createdAt DESC
            """)
    List<DeveloperTask> findRecentOrderedByPriority(Pageable pageable);

    @Query("""
            SELECT task FROM DeveloperTask task
            WHERE task.userId = :userId
            ORDER BY
                CASE
                    WHEN task.priority = com.example.aidevdashboard.model.TaskPriority.HIGH THEN 0
                    WHEN task.priority = com.example.aidevdashboard.model.TaskPriority.MEDIUM THEN 1
                    ELSE 2
                END,
                task.createdAt DESC
            """)
    List<DeveloperTask> findRecentByUserIdOrderedByPriority(Long userId, Pageable pageable);

    java.util.Optional<DeveloperTask> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    @Modifying
    @Query("UPDATE DeveloperTask task SET task.userId = :userId WHERE task.userId IS NULL")
    int assignUnownedToUser(Long userId);

    void deleteByUserId(Long userId);
}
