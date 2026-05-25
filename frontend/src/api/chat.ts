import { apiRequest } from './client';
import type { ChatMessage, ChatResponse, DashboardSummary } from '../types';

export function sendChatMessage(message: string, errorLogId?: number) {
  return apiRequest<ChatResponse>('/chat', {
    method: 'POST',
    body: JSON.stringify({ message, errorLogId: errorLogId ?? null })
  });
}

export function getChatHistory() {
  return apiRequest<ChatMessage[]>('/chat/history');
}

export function getDashboardSummary() {
  return apiRequest<DashboardSummary>('/dashboard/summary');
}
