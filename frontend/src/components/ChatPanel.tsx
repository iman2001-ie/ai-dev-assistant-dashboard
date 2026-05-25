import { FormEvent, useEffect, useState } from 'react';
import { getChatHistory, sendChatMessage } from '../api/chat';
import { getLogs } from '../api/logs';
import type { ChatMessage, ErrorLog } from '../types';

export default function ChatPanel() {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [logs, setLogs] = useState<ErrorLog[]>([]);
  const [message, setMessage] = useState('');
  const [selectedLogId, setSelectedLogId] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([getChatHistory(), getLogs()])
      .then(([history, savedLogs]) => {
        setMessages(history);
        setLogs(savedLogs);
      })
      .catch((err: Error) => setError(err.message));
  }, []);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    if (!message.trim()) {
      return;
    }
    setLoading(true);
    setError('');
    try {
      const response = await sendChatMessage(message, selectedLogId ? Number(selectedLogId) : undefined);
      setMessages((current) => [...current, response.userMessage, response.assistantMessage]);
      setMessage('');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Chat request failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="chat-panel">
      {error && <div className="error-banner">{error}</div>}
      <div className="chat-history">
        {messages.length === 0 && <p className="empty">Ask the assistant about a task, log, or debugging plan.</p>}
        {messages.map((chatMessage) => (
          <article key={chatMessage.id} className={`chat-message ${chatMessage.role.toLowerCase()}`}>
            <strong>{chatMessage.role === 'USER' ? 'You' : 'Assistant'}</strong>
            <p>{chatMessage.content}</p>
          </article>
        ))}
      </div>
      <form className="chat-form" onSubmit={handleSubmit}>
        <select value={selectedLogId} onChange={(event) => setSelectedLogId(event.target.value)}>
          <option value="">No log context</option>
          {logs.map((log) => (
            <option key={log.id} value={log.id}>
              {log.title}
            </option>
          ))}
        </select>
        <textarea
          value={message}
          onChange={(event) => setMessage(event.target.value)}
          placeholder="Ask for debugging help, next steps, or a task summary"
        />
        <button type="submit" disabled={loading}>
          {loading ? 'Thinking...' : 'Send'}
        </button>
      </form>
    </div>
  );
}
