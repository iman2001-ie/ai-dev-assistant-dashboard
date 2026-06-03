import type { DeveloperTask, ErrorLog, TaskPriority } from '../types';

const taskPriorityRank: Record<TaskPriority, number> = {
  HIGH: 3,
  MEDIUM: 2,
  LOW: 1,
};

function newestFirst(left: string, right: string) {
  return new Date(right).getTime() - new Date(left).getTime();
}

export function sortTasksByPriority(tasks: DeveloperTask[]) {
  return [...tasks].sort((left, right) => {
    const priorityDifference = taskPriorityRank[right.priority] - taskPriorityRank[left.priority];
    if (priorityDifference !== 0) return priorityDifference;
    return newestFirst(left.createdAt, right.createdAt);
  });
}

export function sortLogsByStatusAndCreatedAt(logs: ErrorLog[]) {
  return [...logs].sort((left, right) => {
    if (left.resolved !== right.resolved) {
      return left.resolved ? 1 : -1;
    }
    return newestFirst(left.createdAt, right.createdAt);
  });
}
