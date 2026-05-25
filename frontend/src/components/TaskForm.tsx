import { FormEvent, useState } from 'react';
import type { DeveloperTask, TaskPayload, TaskPriority, TaskStatus } from '../types';

interface TaskFormProps {
  initialTask?: DeveloperTask | null;
  onSubmit: (payload: TaskPayload) => Promise<void>;
  onCancel?: () => void;
}

const emptyTask: TaskPayload = {
  title: '',
  description: '',
  status: 'TODO',
  priority: 'MEDIUM'
};

export default function TaskForm({ initialTask, onSubmit, onCancel }: TaskFormProps) {
  const [form, setForm] = useState<TaskPayload>(
    initialTask
      ? {
          title: initialTask.title,
          description: initialTask.description ?? '',
          status: initialTask.status,
          priority: initialTask.priority
        }
      : emptyTask
  );
  const [saving, setSaving] = useState(false);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setSaving(true);
    try {
      await onSubmit(form);
      if (!initialTask) {
        setForm(emptyTask);
      }
    } finally {
      setSaving(false);
    }
  }

  return (
    <form className="stack form" onSubmit={handleSubmit}>
      <label>
        Title
        <input
          required
          value={form.title}
          onChange={(event) => setForm({ ...form, title: event.target.value })}
          placeholder="Fix failing integration test"
        />
      </label>
      <label>
        Description
        <textarea
          value={form.description}
          onChange={(event) => setForm({ ...form, description: event.target.value })}
          placeholder="What needs to be done?"
        />
      </label>
      <div className="form-row">
        <label>
          Status
          <select
            value={form.status}
            onChange={(event) => setForm({ ...form, status: event.target.value as TaskStatus })}
          >
            <option value="TODO">Todo</option>
            <option value="IN_PROGRESS">In progress</option>
            <option value="DONE">Done</option>
          </select>
        </label>
        <label>
          Priority
          <select
            value={form.priority}
            onChange={(event) => setForm({ ...form, priority: event.target.value as TaskPriority })}
          >
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
          </select>
        </label>
      </div>
      <div className="actions">
        <button type="submit" disabled={saving}>
          {saving ? 'Saving...' : initialTask ? 'Update task' : 'Create task'}
        </button>
        {onCancel && (
          <button type="button" className="secondary" onClick={onCancel}>
            Cancel
          </button>
        )}
      </div>
    </form>
  );
}
