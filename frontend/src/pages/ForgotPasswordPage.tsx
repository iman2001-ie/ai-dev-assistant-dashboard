import { Link } from 'react-router-dom';

export default function ForgotPasswordPage() {
  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h1 style={styles.title}>Forgot password</h1>
        <p style={styles.copy}>Password recovery is not available yet.</p>
        <p style={styles.copy}>For now, create a new local test user or reset the local development database.</p>
        <Link to="/login" style={styles.link}>
          Back to login
        </Link>
      </div>
    </div>
  );
}

const styles: { [key: string]: React.CSSProperties } = {
  container: {
    alignItems: 'center',
    background: '#f5f5f5',
    display: 'flex',
    justifyContent: 'center',
    minHeight: '100vh',
  },
  card: {
    background: 'white',
    borderRadius: '8px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
    maxWidth: '420px',
    padding: '2rem',
    width: '100%',
  },
  title: {
    color: '#333',
    marginTop: 0,
  },
  copy: {
    color: '#555',
    lineHeight: 1.5,
  },
  link: {
    color: '#007bff',
    display: 'inline-block',
    fontWeight: 600,
    marginTop: '0.75rem',
    textDecoration: 'none',
  },
};
