import { apiRequest } from './client';
import type { ChatMessage, ChatResponse, DashboardSummary } from '../types';

function chatHistoryPath(errorLogId?: number) {
  const searchParams = new URLSearchParams();
  if (errorLogId === undefined) {
    searchParams.set('noContext', 'true');
  } else {
    searchParams.set('errorLogId', String(errorLogId));
  }
  return `/chat/history?${searchParams.toString()}`;
}

export function sendChatMessage(message: string, errorLogId?: number) {
  return apiRequest<ChatResponse>('/chat', {
    method: 'POST',
    body: JSON.stringify({ message, errorLogId: errorLogId ?? null })
  });
}

export function getChatHistory(errorLogId?: number) {
  return apiRequest<ChatMessage[]>(chatHistoryPath(errorLogId));
}

export function clearChatHistory(errorLogId?: number) {
  return apiRequest<void>(chatHistoryPath(errorLogId), { method: 'DELETE' });
}

export function getDashboardSummary() {
  return apiRequest<DashboardSummary>('/dashboard/summary');
}
