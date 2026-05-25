import { Navigate, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout';
import AssistantPage from './pages/AssistantPage';
import DashboardPage from './pages/DashboardPage';
import LogsPage from './pages/LogsPage';
import TasksPage from './pages/TasksPage';

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<DashboardPage />} />
        <Route path="/tasks" element={<TasksPage />} />
        <Route path="/logs" element={<LogsPage />} />
        <Route path="/assistant" element={<AssistantPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Layout>
  );
}
