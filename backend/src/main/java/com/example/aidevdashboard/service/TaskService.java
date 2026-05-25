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

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> findAll() {
        return taskRepository.findAll().stream().map(TaskResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> findRecent() {
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
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + id));
    }

    private void applyRequest(DeveloperTask task, TaskRequest request) {
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
    }
}
