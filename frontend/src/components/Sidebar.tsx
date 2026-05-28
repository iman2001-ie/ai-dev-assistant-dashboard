import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const links = [
  { to: '/', label: 'Dashboard' },
  { to: '/tasks', label: 'Tasks' },
  { to: '/logs', label: 'Error Logs' },
  { to: '/assistant', label: 'AI Assistant' }
];

export default function Sidebar() {
  const { currentUser, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <aside className="sidebar">
      <div>
        <h1>AI Dev Assistant</h1>
        <p>Developer productivity workspace</p>
      </div>
      <nav>
        {links.map((link) => (
          <NavLink key={link.to} to={link.to} end={link.to === '/'}>
            {link.label}
          </NavLink>
        ))}
      </nav>
      <div style={styles.userSection}>
        <div style={styles.userInfo}>
          <p style={styles.userLabel}>Logged in as</p>
          <p style={styles.userName}>{currentUser}</p>
        </div>
        <button onClick={handleLogout} style={styles.logoutButton}>
          Logout
        </button>
      </div>
    </aside>
  );
}

const styles: { [key: string]: React.CSSProperties } = {
  userSection: {
    marginTop: 'auto',
    paddingTop: '1rem',
    borderTop: '1px solid #ddd',
  },
  userInfo: {
    marginBottom: '1rem',
    paddingBottom: '1rem',
  },
  userLabel: {
    fontSize: '0.75rem',
    color: '#999',
    margin: '0 0 0.25rem 0',
    textTransform: 'uppercase',
    letterSpacing: '0.5px',
  },
  userName: {
    margin: 0,
    fontWeight: 600,
    color: '#333',
  },
  logoutButton: {
    width: '100%',
    padding: '0.5rem',
    background: '#f44336',
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    fontSize: '0.9rem',
    fontWeight: 600,
  },
};
