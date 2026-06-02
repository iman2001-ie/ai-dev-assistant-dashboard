import { type FormEvent, useEffect, useState } from 'react';
import Card from '../components/Card';
import { useAuth } from '../contexts/AuthContext';
import { getProfile, type UserProfile } from '../services/auth';

function errorMessage(err: unknown, fallback: string) {
  if (err instanceof Error) return err.message;
  if (err && typeof err === 'object' && 'message' in err) {
    return String((err as { message?: unknown }).message);
  }
  return fallback;
}

export default function AccountPage() {
  const { currentUser, updateProfile } = useAuth();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    getProfile()
      .then((loadedProfile) => {
        setProfile(loadedProfile);
        setUsername(loadedProfile.username);
        setEmail(loadedProfile.email);
      })
      .catch((err: unknown) => setError(errorMessage(err, 'Could not load account details')));
  }, []);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setSaving(true);
    setError('');
    setMessage('');

    try {
      const updated = await updateProfile({
        username,
        email,
        currentPassword: currentPassword || undefined,
        newPassword: newPassword || undefined,
      });
      setProfile({ username: updated.username, email: updated.email });
      setUsername(updated.username);
      setEmail(updated.email);
      setCurrentPassword('');
      setNewPassword('');
      setMessage('Account updated.');
    } catch (err: unknown) {
      setError(errorMessage(err, 'Could not update account'));
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="stack">
      <header className="page-header">
        <div>
          <h1>Account</h1>
          <p>Manage your sign-in details.</p>
        </div>
      </header>

      {error && <div className="error-banner">{error}</div>}
      {message && <div className="success-banner">{message}</div>}

      <div className="account-grid">
        <section className="account-summary">
          <div className="account-avatar">{(profile?.username ?? currentUser ?? 'U').slice(0, 1).toUpperCase()}</div>
          <div>
            <h2>{profile?.username ?? currentUser}</h2>
            <p>{profile?.email ?? 'Loading account details...'}</p>
          </div>
        </section>

        <Card title="Profile">
          <form className="form stack" onSubmit={handleSubmit}>
            <label>
              Username
              <input value={username} onChange={(event) => setUsername(event.target.value)} required />
            </label>
            <label>
              Email
              <input type="email" value={email} onChange={(event) => setEmail(event.target.value)} required />
            </label>
            <div className="form-row">
              <label>
                Current password
                <input
                  type="password"
                  value={currentPassword}
                  onChange={(event) => setCurrentPassword(event.target.value)}
                  autoComplete="current-password"
                />
              </label>
              <label>
                New password
                <input
                  type="password"
                  value={newPassword}
                  onChange={(event) => setNewPassword(event.target.value)}
                  autoComplete="new-password"
                />
              </label>
            </div>
            <div className="actions">
              <button type="submit" disabled={saving || !username || !email}>
                {saving ? 'Saving...' : 'Save account'}
              </button>
            </div>
          </form>
        </Card>
      </div>
    </div>
  );
}
