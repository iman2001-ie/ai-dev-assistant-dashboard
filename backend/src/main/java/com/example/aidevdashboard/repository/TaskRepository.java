package com.example.aidevdashboard.repository;

import com.example.aidevdashboard.model.DeveloperTask;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<DeveloperTask, Long> {
    List<DeveloperTask> findTop5ByOrderByCreatedAtDesc();
}
