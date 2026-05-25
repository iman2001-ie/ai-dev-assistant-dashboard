import { FormEvent, useState } from 'react';
import type { ErrorLog, LogPayload } from '../types';

interface LogFormProps {
  initialLog?: ErrorLog | null;
  onSubmit: (payload: LogPayload) => Promise<void>;
  onCancel?: () => void;
}

const emptyLog: LogPayload = {
  title: '',
  content: '',
  source: '',
  resolved: false
};

export default function LogForm({ initialLog, onSubmit, onCancel }: LogFormProps) {
  const [form, setForm] = useState<LogPayload>(
    initialLog
      ? {
          title: initialLog.title,
          content: initialLog.content,
          source: initialLog.source ?? '',
          resolved: initialLog.resolved
        }
      : emptyLog
  );
  const [saving, setSaving] = useState(false);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setSaving(true);
    try {
      await onSubmit(form);
      if (!initialLog) {
        setForm(emptyLog);
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
          placeholder="Database connection refused"
        />
      </label>
      <label>
        Source
        <input
          value={form.source}
          onChange={(event) => setForm({ ...form, source: event.target.value })}
          placeholder="backend, frontend, CI"
        />
      </label>
      <label>
        Error log
        <textarea
          required
          className="log-textarea"
          value={form.content}
          onChange={(event) => setForm({ ...form, content: event.target.value })}
          placeholder="Paste stack trace or error output"
        />
      </label>
      <label className="checkbox">
        <input
          type="checkbox"
          checked={form.resolved}
          onChange={(event) => setForm({ ...form, resolved: event.target.checked })}
        />
        Resolved
      </label>
      <div className="actions">
        <button type="submit" disabled={saving}>
          {saving ? 'Saving...' : initialLog ? 'Update log' : 'Save log'}
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
