import { useEffect, useState } from 'react';
import { createTask, deleteTask, getTasks, updateTask } from '../api/tasks';
import Card from '../components/Card';
import TaskForm from '../components/TaskForm';
import TaskList from '../components/TaskList';
import type { DeveloperTask, TaskPayload } from '../types';
import { sortTasksByPriority } from '../utils/sort';

export default function TasksPage() {
  const [tasks, setTasks] = useState<DeveloperTask[]>([]);
  const [editingTask, setEditingTask] = useState<DeveloperTask | null>(null);
  const [statusFilter, setStatusFilter] = useState<'ALL' | DeveloperTask['status']>('ALL');
  const [priorityFilter, setPriorityFilter] = useState<'ALL' | DeveloperTask['priority']>('ALL');
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [messageFading, setMessageFading] = useState(false);

  async function loadTasks() {
    setTasks(sortTasksByPriority(await getTasks()));
  }

  useEffect(() => {
    loadTasks().catch((err: Error) => setError(err.message));
  }, []);

  useEffect(() => {
    if (!message) return;
    setMessageFading(false);
    const fadeTimeoutId = window.setTimeout(() => setMessageFading(true), 3200);
    const clearTimeoutId = window.setTimeout(() => setMessage(''), 4300);
    return () => {
      window.clearTimeout(fadeTimeoutId);
      window.clearTimeout(clearTimeoutId);
    };
  }, [message]);

  async function handleCreate(payload: TaskPayload) {
    setError('');
    setMessage('');
    setMessageFading(false);
    try {
      await createTask(payload);
      await loadTasks();
      setMessage('New task created.');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not create task');
      throw err;
    }
  }

  async function handleUpdate(payload: TaskPayload) {
    if (!editingTask) {
      return;
    }
    setError('');
    setMessage('');
    setMessageFading(false);
    try {
      await updateTask(editingTask.id, payload);
      setEditingTask(null);
      await loadTasks();
      setMessage('Task updated.');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not update task');
      throw err;
    }
  }

  async function handleDelete(id: number) {
    setError('');
    setMessage('');
    setMessageFading(false);
    try {
      await deleteTask(id);
      await loadTasks();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not delete task');
    }
  }

  const visibleTasks = tasks.filter((task) => {
    const matchesStatus = statusFilter === 'ALL' || task.status === statusFilter;
    const matchesPriority = priorityFilter === 'ALL' || task.priority === priorityFilter;
    return matchesStatus && matchesPriority;
  });

  return (
    <div className="page stack">
      <header className="page-header">
        <div>
          <h1>Tasks</h1>
          <p>Create and manage coding tasks.</p>
        </div>
      </header>
      {error && <div className="error-banner">{error}</div>}
      {message && <div className={`success-banner ${messageFading ? 'fade-out' : ''}`}>{message}</div>}
      <div className="content-grid">
        <Card title={editingTask ? 'Edit task' : 'New task'}>
          <TaskForm
            key={editingTask?.id ?? 'new-task'}
            initialTask={editingTask}
            onSubmit={editingTask ? handleUpdate : handleCreate}
            onCancel={editingTask ? () => setEditingTask(null) : undefined}
          />
        </Card>
        <Card title="Task list">
          <div className="toolbar">
            <label>
              Status
              <select
                value={statusFilter}
                onChange={(event) => setStatusFilter(event.target.value as typeof statusFilter)}
              >
                <option value="ALL">All</option>
                <option value="TODO">Todo</option>
                <option value="IN_PROGRESS">In progress</option>
                <option value="DONE">Done</option>
              </select>
            </label>
            <label>
              Priority
              <select
                value={priorityFilter}
                onChange={(event) => setPriorityFilter(event.target.value as typeof priorityFilter)}
              >
                <option value="ALL">All</option>
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
              </select>
            </label>
          </div>
          <TaskList tasks={visibleTasks} onEdit={setEditingTask} onDelete={handleDelete} />
        </Card>
      </div>
    </div>
  );
}
