import type { ErrorLog } from '../types';

interface LogListProps {
  logs: ErrorLog[];
  onEdit?: (log: ErrorLog) => void;
  onDelete?: (id: number) => void;
}

export default function LogList({ logs, onEdit, onDelete }: LogListProps) {
  if (logs.length === 0) {
    return <p className="empty">No saved logs yet.</p>;
  }

  return (
    <div className="list">
      {logs.map((log) => (
        <article key={log.id} className="list-item">
          <div>
            <div className="item-header">
              <h3>{log.title}</h3>
              <span className={`pill ${log.resolved ? 'done' : 'high'}`}>
                {log.resolved ? 'Resolved' : 'Open'}
              </span>
            </div>
            <small>{log.source || 'Unknown source'}</small>
            <pre>{log.content.slice(0, 220)}{log.content.length > 220 ? '...' : ''}</pre>
          </div>
          {(onEdit || onDelete) && (
            <div className="item-actions">
              {onEdit && (
                <button type="button" className="secondary" onClick={() => onEdit(log)}>
                  Edit
                </button>
              )}
              {onDelete && (
                <button type="button" className="danger" onClick={() => onDelete(log.id)}>
                  Delete
                </button>
              )}
            </div>
          )}
        </article>
      ))}
    </div>
  );
}
