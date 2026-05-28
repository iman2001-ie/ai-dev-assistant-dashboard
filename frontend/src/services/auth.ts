export function getToken(): string | null {
  return localStorage.getItem('token');
}

export function getRefreshToken(): string | null {
  return localStorage.getItem('refreshToken');
}

export function setTokens(token: string, refreshToken: string | null) {
  localStorage.setItem('token', token);
  if (refreshToken) localStorage.setItem('refreshToken', refreshToken);
}

export function clearTokens() {
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api';
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
async function postJson(path: string, body: any) {
  const res = await fetch(`${API_BASE_URL}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  const json = await res.json().catch(() => null);
  return { res, json };
}

export async function register(username: string, email: string, password: string) {
  const { res, json } = await postJson('/auth/register', { username, email, password });
  if (!res.ok) throw json || { message: 'Register failed' };
  setTokens(json.token, json.refreshToken);
  return json;
}

export async function login(username: string, password: string) {
  const { res, json } = await postJson('/auth/login', { username, password });
  if (!res.ok) throw json || { message: 'Login failed' };
  setTokens(json.token, json.refreshToken);
  return json;
}

export async function refresh() {
  const refreshToken = getRefreshToken();
  if (!refreshToken) throw new Error('No refresh token');
  const { res, json } = await postJson('/auth/refresh', { refreshToken });
  if (!res.ok) {
    clearTokens();
    throw json || { message: 'Refresh failed' };
  }
  setTokens(json.token, json.refreshToken);
  return json;
}

export async function logout() {
  const token = getToken();
  try {
    await fetch(`${API_BASE_URL}/auth/logout`, {
      method: 'POST',
      headers: token ? { Authorization: `Bearer ${token}` } : undefined,
    });
  } catch {
    // ignore network errors
  }
  clearTokens();
}

export async function fetchWithAuth(input: RequestInfo, init: RequestInit = {}) {
  let token = getToken();
  const headers = new Headers(init.headers || {});
  if (token) headers.set('Authorization', 'Bearer ' + token);
  init.headers = headers;

  let res = await fetch(input, init);
  if (res.status === 401) {
    try {
      await refresh();
      token = getToken();
      if (token) headers.set('Authorization', 'Bearer ' + token);
      init.headers = headers;
      res = await fetch(input, init);
    } catch {
      // refresh failed
    }
  }
  return res;
}
