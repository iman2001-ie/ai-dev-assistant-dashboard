import { FormEvent, useEffect, useState } from 'react';
import { clearChatHistory, getChatHistory, sendChatMessage } from '../api/chat';
import { getLogs } from '../api/logs';
import type { ChatMessage, ErrorLog } from '../types';
import MarkdownMessage from './MarkdownMessage';

export default function ChatPanel() {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [logs, setLogs] = useState<ErrorLog[]>([]);
  const [message, setMessage] = useState('');
  const [selectedLogId, setSelectedLogId] = useState('');
  const [loading, setLoading] = useState(false);
  const [clearing, setClearing] = useState(false);
  const [confirmClear, setConfirmClear] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    getLogs()
      .then((savedLogs) => {
        setLogs(savedLogs);
      })
      .catch((err: Error) => setError(err.message));
  }, []);

  useEffect(() => {
    setError('');
    setConfirmClear(false);
    getChatHistory(selectedLogId ? Number(selectedLogId) : undefined)
      .then(setMessages)
      .catch((err: Error) => setError(err.message));
  }, [selectedLogId]);

  const selectedLog = selectedLogId ? logs.find((log) => log.id === Number(selectedLogId)) : null;
  const contextName = selectedLog?.title ?? 'General chat';
  const contextDescription = selectedLog
    ? `Chat history for "${selectedLog.title}"`
    : 'Chat history without an attached error log';

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

  async function handleClearHistory() {
    if (!confirmClear) {
      setConfirmClear(true);
      return;
    }

    setClearing(true);
    setError('');
    try {
      await clearChatHistory(selectedLogId ? Number(selectedLogId) : undefined);
      setMessages([]);
      setConfirmClear(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not clear chat history');
    } finally {
      setClearing(false);
    }
  }

  return (
    <div className="chat-panel">
      {error && <div className="error-banner">{error}</div>}
      <section className="context-panel" aria-label="Conversation context">
        <label>
          Conversation context
          <select value={selectedLogId} onChange={(event) => setSelectedLogId(event.target.value)}>
            <option value="">General chat</option>
            {logs.map((log) => (
              <option key={log.id} value={log.id}>
                {log.title}
              </option>
            ))}
          </select>
        </label>
        <p>{contextDescription}</p>
      </section>
      {messages.length > 0 && (
        <div className="chat-toolbar">
          <span className="context-pill">{contextName}</span>
          <button
            type="button"
            className={confirmClear ? 'danger' : 'secondary'}
            disabled={loading || clearing}
            onClick={handleClearHistory}
          >
            {clearing ? 'Clearing...' : confirmClear ? 'Confirm clear' : 'Clear history'}
          </button>
          {confirmClear && (
            <button type="button" className="secondary" disabled={clearing} onClick={() => setConfirmClear(false)}>
              Cancel
            </button>
          )}
        </div>
      )}
      <div className="chat-history">
        {messages.length === 0 && <p className="empty">Ask the assistant about this context.</p>}
        {messages.map((chatMessage) => (
          <article key={chatMessage.id} className={`chat-message ${chatMessage.role.toLowerCase()}`}>
            <strong>{chatMessage.role === 'USER' ? 'You' : 'Assistant'}</strong>
            {chatMessage.role === 'ASSISTANT' ? (
              <MarkdownMessage content={chatMessage.content} />
            ) : (
              <p>{chatMessage.content}</p>
            )}
          </article>
        ))}
      </div>
      <form className="chat-form" onSubmit={handleSubmit}>
        <textarea
          value={message}
          onChange={(event) => setMessage(event.target.value)}
          placeholder={selectedLog ? `Ask about ${selectedLog.title}` : 'Ask a general coding question'}
        />
        <button type="submit" disabled={loading}>
          {loading ? 'Thinking...' : 'Send'}
        </button>
      </form>
    </div>
  );
}
