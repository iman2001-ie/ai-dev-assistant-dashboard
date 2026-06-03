import { useEffect, useState } from 'react';
import { createLog, deleteLog, getLogs, updateLog } from '../api/logs';
import Card from '../components/Card';
import LogForm from '../components/LogForm';
import LogList from '../components/LogList';
import type { ErrorLog, LogPayload } from '../types';
import { sortLogsByStatusAndCreatedAt } from '../utils/sort';

export default function LogsPage() {
  const [logs, setLogs] = useState<ErrorLog[]>([]);
  const [editingLog, setEditingLog] = useState<ErrorLog | null>(null);
  const [resolvedFilter, setResolvedFilter] = useState<'ALL' | 'OPEN' | 'RESOLVED'>('ALL');
  const [sourceFilter, setSourceFilter] = useState('');
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [messageFading, setMessageFading] = useState(false);

  async function loadLogs() {
    setLogs(sortLogsByStatusAndCreatedAt(await getLogs()));
  }

  useEffect(() => {
    loadLogs().catch((err: Error) => setError(err.message));
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

  async function handleCreate(payload: LogPayload) {
    setError('');
    setMessage('');
    setMessageFading(false);
    try {
      await createLog(payload);
      await loadLogs();
      setMessage('New log created.');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not save log');
      throw err;
    }
  }

  async function handleUpdate(payload: LogPayload) {
    if (!editingLog) {
      return;
    }
    setError('');
    setMessage('');
    setMessageFading(false);
    try {
      await updateLog(editingLog.id, payload);
      setEditingLog(null);
      await loadLogs();
      setMessage('Log updated.');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not update log');
      throw err;
    }
  }

  async function handleDelete(id: number) {
    setError('');
    setMessage('');
    setMessageFading(false);
    try {
      await deleteLog(id);
      await loadLogs();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not delete log');
    }
  }

  const sources = Array.from(new Set(logs.map((log) => log.source).filter((source): source is string => Boolean(source)))).sort();
  const visibleLogs = logs.filter((log) => {
    const matchesResolved =
      resolvedFilter === 'ALL' ||
      (resolvedFilter === 'OPEN' && !log.resolved) ||
      (resolvedFilter === 'RESOLVED' && log.resolved);
    const matchesSource = !sourceFilter || log.source === sourceFilter;
    return matchesResolved && matchesSource;
  });

  return (
    <div className="page stack">
      <header className="page-header">
        <div>
          <h1>Error Logs</h1>
          <p>Save stack traces and attach them to AI questions.</p>
        </div>
      </header>
      {error && <div className="error-banner">{error}</div>}
      {message && <div className={`success-banner ${messageFading ? 'fade-out' : ''}`}>{message}</div>}
      <div className="content-grid">
        <Card title={editingLog ? 'Edit log' : 'New log'}>
          <LogForm
            key={editingLog?.id ?? 'new-log'}
            initialLog={editingLog}
            onSubmit={editingLog ? handleUpdate : handleCreate}
            onCancel={editingLog ? () => setEditingLog(null) : undefined}
          />
        </Card>
        <Card title="Saved logs">
          <div className="toolbar">
            <label>
              Status
              <select
                value={resolvedFilter}
                onChange={(event) => setResolvedFilter(event.target.value as typeof resolvedFilter)}
              >
                <option value="ALL">All</option>
                <option value="OPEN">Open</option>
                <option value="RESOLVED">Resolved</option>
              </select>
            </label>
            <label>
              Source
              <select value={sourceFilter} onChange={(event) => setSourceFilter(event.target.value)}>
                <option value="">All</option>
                {sources.map((source) => (
                  <option key={source} value={source}>
                    {source}
                  </option>
                ))}
              </select>
            </label>
          </div>
          <LogList logs={visibleLogs} onEdit={setEditingLog} onDelete={handleDelete} />
        </Card>
      </div>
    </div>
  );
}
