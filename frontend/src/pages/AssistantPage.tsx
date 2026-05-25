import Card from '../components/Card';
import ChatPanel from '../components/ChatPanel';

export default function AssistantPage() {
  return (
    <div className="page stack">
      <header className="page-header">
        <div>
          <h1>AI Assistant</h1>
          <p>Ask for debugging help with optional saved log context.</p>
        </div>
      </header>
      <Card>
        <ChatPanel />
      </Card>
    </div>
  );
}
