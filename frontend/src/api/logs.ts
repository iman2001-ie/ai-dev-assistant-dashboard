import { apiRequest } from './client';
import type { ErrorLog, LogPayload } from '../types';

export function getLogs() {
  return apiRequest<ErrorLog[]>('/logs');
}

export function getUnresolvedLogs() {
  return apiRequest<ErrorLog[]>('/logs/unresolved');
}

export function createLog(payload: LogPayload) {
  return apiRequest<ErrorLog>('/logs', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function updateLog(id: number, payload: LogPayload) {
  return apiRequest<ErrorLog>(`/logs/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  });
}

export function deleteLog(id: number) {
  return apiRequest<void>(`/logs/${id}`, { method: 'DELETE' });
}
