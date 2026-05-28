package com.example.aidevdashboard.service;

import com.example.aidevdashboard.dto.TaskRequest;
import com.example.aidevdashboard.dto.TaskResponse;
import com.example.aidevdashboard.model.DeveloperTask;
import com.example.aidevdashboard.model.User;
import com.example.aidevdashboard.repository.TaskRepository;
import com.example.aidevdashboard.repository.UserRepository;
import java.util.List;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> findAll() {
        Long userId = resolveCurrentUserId();
        if (userId != null) {
            return taskRepository.findAll().stream()
                    .filter(task -> task.getUserId() == null || userId.equals(task.getUserId()))
                    .map(TaskResponse::fromEntity)
                    .toList();
        }
        return taskRepository.findAll().stream().map(TaskResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> findRecent() {
        Long userId = resolveCurrentUserId();
        if (userId != null) {
            return taskRepository.findAll().stream()
                    .filter(task -> task.getUserId() == null || userId.equals(task.getUserId()))
                    .sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
                    .limit(5)
                    .map(TaskResponse::fromEntity)
                    .toList();
        }
        return taskRepository.findTop5ByOrderByCreatedAtDesc().stream().map(TaskResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse findById(Long id) {
        return TaskResponse.fromEntity(getEntity(id));
    }

    @Transactional
    public TaskResponse create(TaskRequest request) {
        DeveloperTask task = new DeveloperTask();
        applyRequest(task, request);
        Long userId = resolveCurrentUserId();
        if (userId != null) task.setUserId(userId);
        return TaskResponse.fromEntity(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        DeveloperTask task = getEntity(id);
        applyRequest(task, request);
        return TaskResponse.fromEntity(taskRepository.save(task));
    }

    @Transactional
    public void delete(Long id) {
        DeveloperTask task = getEntity(id);
        taskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    public long count() {
        return taskRepository.count();
    }

    private DeveloperTask getEntity(Long id) {
        Long userId = resolveCurrentUserId();
        var maybe = taskRepository.findById(id);
        if (maybe.isEmpty()) {
            throw new ResourceNotFoundException("Task not found with id " + id);
        }
        DeveloperTask task = maybe.get();
        if (userId != null && task.getUserId() != null && !userId.equals(task.getUserId())) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied to task " + id);
        }
        return task;
    }

    private void applyRequest(DeveloperTask task, TaskRequest request) {
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
    }

    private Long resolveCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        String username = auth.getName();
        if (username == null) return null;
        return userRepository.findByUsername(username).map(User::getId).orElse(null);
    }
}
