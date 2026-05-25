export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH';
export type ChatRole = 'USER' | 'ASSISTANT';

export interface DeveloperTask {
  id: number;
  title: string;
  description: string | null;
  status: TaskStatus;
  priority: TaskPriority;
  createdAt: string;
  updatedAt: string;
}

export interface TaskPayload {
  title: string;
  description: string;
  status: TaskStatus;
  priority: TaskPriority;
}

export interface ErrorLog {
  id: number;
  title: string;
  content: string;
  source: string | null;
  resolved: boolean;
  createdAt: string;
}

export interface LogPayload {
  title: string;
  content: string;
  source: string;
  resolved: boolean;
}

export interface ChatMessage {
  id: number;
  role: ChatRole;
  content: string;
  errorLogId: number | null;
  createdAt: string;
}

export interface ChatResponse {
  userMessage: ChatMessage;
  assistantMessage: ChatMessage;
}

export interface DashboardSummary {
  taskCount: number;
  unresolvedLogCount: number;
  savedConversationCount: number;
  recentTasks: DeveloperTask[];
  recentLogs: ErrorLog[];
}
