package com.example.aidevdashboard.service;

import com.example.aidevdashboard.dto.TaskRequest;
import com.example.aidevdashboard.dto.TaskResponse;
import com.example.aidevdashboard.model.DeveloperTask;
import com.example.aidevdashboard.repository.TaskRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final CurrentUserService currentUserService;

    public TaskService(TaskRepository taskRepository, CurrentUserService currentUserService) {
        this.taskRepository = taskRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> findAll() {
        Long userId = currentUserService.currentUserId();
        if (userId != null) {
            return taskRepository.findAllByUserId(userId).stream().map(TaskResponse::fromEntity).toList();
        }
        return taskRepository.findAll().stream().map(TaskResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> findRecent() {
        Long userId = currentUserService.currentUserId();
        if (userId != null) {
            return taskRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId).stream().map(TaskResponse::fromEntity).toList();
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
        Long userId = currentUserService.currentUserId();
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
        Long userId = currentUserService.currentUserId();
        if (userId != null) {
            return taskRepository.countByUserId(userId);
        }
        return taskRepository.count();
    }

    private DeveloperTask getEntity(Long id) {
        Long userId = currentUserService.currentUserId();
        var maybe = taskRepository.findById(id);
        if (maybe.isEmpty()) {
            throw new ResourceNotFoundException("Task not found with id " + id);
        }
        DeveloperTask task = maybe.get();
        if (userId != null && !userId.equals(task.getUserId())) {
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
}
