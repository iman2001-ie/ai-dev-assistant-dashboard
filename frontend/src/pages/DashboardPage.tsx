import { useEffect, useState } from 'react';
import { getDashboardSummary } from '../api/chat';
import Card from '../components/Card';
import LogList from '../components/LogList';
import TaskList from '../components/TaskList';
import type { DashboardSummary } from '../types';

export default function DashboardPage() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    getDashboardSummary()
      .then(setSummary)
      .catch((err: Error) => setError(err.message));
  }, []);

  if (error) {
    return <div className="error-banner">{error}</div>;
  }

  return (
    <div className="page stack">
      <header className="page-header">
        <div>
          <h1>Dashboard</h1>
          <p>Track developer tasks, open logs, and AI assistant activity.</p>
        </div>
      </header>

      <div className="summary-grid">
        <Card>
          <span className="metric">{summary?.taskCount ?? '-'}</span>
          <p>Total tasks</p>
        </Card>
        <Card>
          <span className="metric">{summary?.unresolvedLogCount ?? '-'}</span>
          <p>Unresolved logs</p>
        </Card>
        <Card>
          <span className="metric">{summary?.savedConversationCount ?? '-'}</span>
          <p>Saved AI conversations</p>
        </Card>
      </div>

      <div className="content-grid">
        <Card title="Recent tasks">
          <TaskList tasks={summary?.recentTasks ?? []} />
        </Card>
        <Card title="Recent logs">
          <LogList logs={summary?.recentLogs ?? []} />
        </Card>
      </div>
    </div>
  );
}
