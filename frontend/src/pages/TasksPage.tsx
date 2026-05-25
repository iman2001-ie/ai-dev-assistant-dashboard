import { useEffect, useState } from 'react';
import { createTask, deleteTask, getTasks, updateTask } from '../api/tasks';
import Card from '../components/Card';
import TaskForm from '../components/TaskForm';
import TaskList from '../components/TaskList';
import type { DeveloperTask, TaskPayload } from '../types';

export default function TasksPage() {
  const [tasks, setTasks] = useState<DeveloperTask[]>([]);
  const [editingTask, setEditingTask] = useState<DeveloperTask | null>(null);
  const [error, setError] = useState('');

  async function loadTasks() {
    setTasks(await getTasks());
  }

  useEffect(() => {
    loadTasks().catch((err: Error) => setError(err.message));
  }, []);

  async function handleCreate(payload: TaskPayload) {
    await createTask(payload);
    await loadTasks();
  }

  async function handleUpdate(payload: TaskPayload) {
    if (!editingTask) {
      return;
    }
    await updateTask(editingTask.id, payload);
    setEditingTask(null);
    await loadTasks();
  }

  async function handleDelete(id: number) {
    await deleteTask(id);
    await loadTasks();
  }

  return (
    <div className="page stack">
      <header className="page-header">
        <div>
          <h1>Tasks</h1>
          <p>Create and manage coding tasks.</p>
        </div>
      </header>
      {error && <div className="error-banner">{error}</div>}
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
          <TaskList tasks={tasks} onEdit={setEditingTask} onDelete={handleDelete} />
        </Card>
      </div>
    </div>
  );
}
