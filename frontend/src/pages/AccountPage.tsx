import { type FormEvent, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
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
  const { currentUser, updateProfile, deleteAccount } = useAuth();
  const navigate = useNavigate();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [changePassword, setChangePassword] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(false);
  const [deleteConfirmation, setDeleteConfirmation] = useState('');
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

    if (changePassword && (!currentPassword || !newPassword)) {
      setError('Enter your current password and a new password to change your password.');
      setSaving(false);
      return;
    }

    try {
      const updated = await updateProfile({
        username,
        email,
        currentPassword: changePassword ? currentPassword : undefined,
        newPassword: changePassword ? newPassword : undefined,
      });
      setProfile({ username: updated.username, email: updated.email });
      setUsername(updated.username);
      setEmail(updated.email);
      setCurrentPassword('');
      setNewPassword('');
      setChangePassword(false);
      setMessage('Account updated.');
    } catch (err: unknown) {
      setError(errorMessage(err, 'Could not update account'));
    } finally {
      setSaving(false);
    }
  }

  async function handleDeleteAccount() {
    setDeleting(true);
    setError('');
    setMessage('');

    try {
      await deleteAccount();
      navigate('/login');
    } catch (err: unknown) {
      setError(errorMessage(err, 'Could not delete account'));
      setDeleting(false);
    }
  }

  const expectedConfirmation = profile?.username ?? currentUser ?? '';
  const canDelete = deleteConfirmation === expectedConfirmation;

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

        <div className="account-main">
          <Card title="Profile">
            <form className="form stack" onSubmit={handleSubmit} autoComplete="off">
              <label>
                Username
                <input value={username} onChange={(event) => setUsername(event.target.value)} required />
              </label>
              <label>
                Email
                <input type="email" value={email} onChange={(event) => setEmail(event.target.value)} required />
              </label>
              {!changePassword ? (
                <button className="secondary account-button" type="button" onClick={() => setChangePassword(true)}>
                  Change password
                </button>
              ) : (
                <div className="password-panel">
                  <div className="form-row">
                    <label>
                      Current password
                      <input
                        type="password"
                        value={currentPassword}
                        onChange={(event) => setCurrentPassword(event.target.value)}
                        autoComplete="off"
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
                  <button
                    className="secondary account-button"
                    type="button"
                    onClick={() => {
                      setChangePassword(false);
                      setCurrentPassword('');
                      setNewPassword('');
                    }}
                  >
                    Cancel password change
                  </button>
                </div>
              )}
              <div className="actions">
                <button className="account-button" type="submit" disabled={saving || !username || !email}>
                  {saving ? 'Saving...' : 'Save account'}
                </button>
              </div>
            </form>
          </Card>

          <Card title="Danger zone">
            <div className="stack">
              {!confirmDelete ? (
                <>
                  <p className="muted-copy">Delete this account and remove its local tasks, logs, and chat history.</p>
                  <button className="danger account-button" type="button" onClick={() => setConfirmDelete(true)}>
                    Delete account
                  </button>
                </>
              ) : (
                <>
                  <div className="warning-panel">
                    <strong>This cannot be undone.</strong>
                    <p>Type <span>{expectedConfirmation}</span> to confirm account deletion.</p>
                  </div>
                  <label className="danger-confirm-label">
                    Confirm username
                    <input
                      value={deleteConfirmation}
                      onChange={(event) => setDeleteConfirmation(event.target.value)}
                      disabled={deleting}
                    />
                  </label>
                  <div className="actions">
                    <button className="danger account-button" type="button" disabled={!canDelete || deleting} onClick={handleDeleteAccount}>
                      {deleting ? 'Deleting...' : 'Confirm delete'}
                    </button>
                    <button
                      className="secondary account-button"
                      type="button"
                      disabled={deleting}
                      onClick={() => {
                        setConfirmDelete(false);
                        setDeleteConfirmation('');
                      }}
                    >
                      Cancel
                    </button>
                  </div>
                </>
              )}
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}
