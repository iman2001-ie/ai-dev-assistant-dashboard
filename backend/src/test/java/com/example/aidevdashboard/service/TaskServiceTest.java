package com.example.aidevdashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.aidevdashboard.dto.TaskRequest;
import com.example.aidevdashboard.dto.TaskResponse;
import com.example.aidevdashboard.model.DeveloperTask;
import com.example.aidevdashboard.model.TaskPriority;
import com.example.aidevdashboard.model.TaskStatus;
import com.example.aidevdashboard.repository.TaskRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void createPersistsTaskFromRequest() {
        TaskRequest request = new TaskRequest("Fix build", "Vite build fails", TaskStatus.TODO, TaskPriority.HIGH);
        when(taskRepository.save(org.mockito.Mockito.any(DeveloperTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.create(request);

        ArgumentCaptor<DeveloperTask> captor = ArgumentCaptor.forClass(DeveloperTask.class);
        verify(taskRepository).save(captor.capture());
        DeveloperTask savedTask = captor.getValue();
        assertThat(savedTask.getTitle()).isEqualTo("Fix build");
        assertThat(savedTask.getDescription()).isEqualTo("Vite build fails");
        assertThat(savedTask.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(savedTask.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(response.title()).isEqualTo("Fix build");
    }

    @Test
    void updateThrowsWhenTaskDoesNotExist() {
        when(taskRepository.findById(42L)).thenReturn(Optional.empty());
        TaskRequest request = new TaskRequest("Missing", "", TaskStatus.TODO, TaskPriority.MEDIUM);

        assertThatThrownBy(() -> taskService.update(42L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("42");
    }
}
