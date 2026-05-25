import { useEffect, useState } from 'react';
import { createLog, deleteLog, getLogs, updateLog } from '../api/logs';
import Card from '../components/Card';
import LogForm from '../components/LogForm';
import LogList from '../components/LogList';
import type { ErrorLog, LogPayload } from '../types';

export default function LogsPage() {
  const [logs, setLogs] = useState<ErrorLog[]>([]);
  const [editingLog, setEditingLog] = useState<ErrorLog | null>(null);
  const [error, setError] = useState('');

  async function loadLogs() {
    setLogs(await getLogs());
  }

  useEffect(() => {
    loadLogs().catch((err: Error) => setError(err.message));
  }, []);

  async function handleCreate(payload: LogPayload) {
    await createLog(payload);
    await loadLogs();
  }

  async function handleUpdate(payload: LogPayload) {
    if (!editingLog) {
      return;
    }
    await updateLog(editingLog.id, payload);
    setEditingLog(null);
    await loadLogs();
  }

  async function handleDelete(id: number) {
    await deleteLog(id);
    await loadLogs();
  }

  return (
    <div className="page stack">
      <header className="page-header">
        <div>
          <h1>Error Logs</h1>
          <p>Save stack traces and attach them to AI questions.</p>
        </div>
      </header>
      {error && <div className="error-banner">{error}</div>}
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
          <LogList logs={logs} onEdit={setEditingLog} onDelete={handleDelete} />
        </Card>
      </div>
    </div>
  );
}
