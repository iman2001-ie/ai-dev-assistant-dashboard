import { apiRequest } from './client';
import type { DeveloperTask, TaskPayload } from '../types';

export function getTasks() {
  return apiRequest<DeveloperTask[]>('/tasks');
}

export function createTask(payload: TaskPayload) {
  return apiRequest<DeveloperTask>('/tasks', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function updateTask(id: number, payload: TaskPayload) {
  return apiRequest<DeveloperTask>(`/tasks/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  });
}

export function deleteTask(id: number) {
  return apiRequest<void>(`/tasks/${id}`, { method: 'DELETE' });
}
