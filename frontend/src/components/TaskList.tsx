import type { DeveloperTask } from '../types';

interface TaskListProps {
  tasks: DeveloperTask[];
  onEdit?: (task: DeveloperTask) => void;
  onDelete?: (id: number) => void;
}

export default function TaskList({ tasks, onEdit, onDelete }: TaskListProps) {
  if (tasks.length === 0) {
    return <p className="empty">No tasks yet.</p>;
  }

  return (
    <div className="list">
      {tasks.map((task) => (
        <article key={task.id} className="list-item">
          <div>
            <div className="item-header">
              <h3>{task.title}</h3>
              <span className={`pill ${task.priority.toLowerCase()}`}>{task.priority}</span>
            </div>
            {task.description && <p>{task.description}</p>}
            <small>{task.status.replace('_', ' ')}</small>
          </div>
          {(onEdit || onDelete) && (
            <div className="item-actions">
              {onEdit && (
                <button type="button" className="secondary" onClick={() => onEdit(task)}>
                  Edit
                </button>
              )}
              {onDelete && (
                <button type="button" className="danger" onClick={() => onDelete(task.id)}>
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
