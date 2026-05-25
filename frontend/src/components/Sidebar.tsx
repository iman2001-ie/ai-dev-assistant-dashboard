import { NavLink } from 'react-router-dom';

const links = [
  { to: '/', label: 'Dashboard' },
  { to: '/tasks', label: 'Tasks' },
  { to: '/logs', label: 'Error Logs' },
  { to: '/assistant', label: 'AI Assistant' }
];

export default function Sidebar() {
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
    </aside>
  );
}
