# Authentication Decision

## Status

Accepted for the current MVP.

## Decision

The app uses stateless JWT access tokens for API authentication, plus server-side
refresh tokens stored on the `users` table. It does not use server-backed HTTP
sessions for normal app requests.

## Rationale

This project is a small React + Spring Boot MVP. JWT access tokens keep the
frontend and backend contract simple: the frontend sends `Authorization: Bearer
<token>` for protected API calls, and the backend validates that token on each
request.

Refresh tokens are stored server-side so logout and token rotation can invalidate
future refresh attempts without introducing a full session store. This keeps the
implementation beginner-friendly while still giving the app a clear path for
login, refresh, and logout flows.

## Current Contract

- Public auth endpoints:
  - `POST /api/auth/register`
  - `POST /api/auth/login`
  - `POST /api/auth/refresh`
- Protected auth endpoint:
  - `POST /api/auth/logout`
- Protected app endpoints require an `Authorization: Bearer <jwt>` header.
- Access tokens are signed by the backend using `app.jwt.secret`.
- Access token lifetime is controlled by `app.jwt.expiration-seconds`.
- Refresh tokens are opaque UUID values stored with the user record.
- Login and refresh rotate the refresh token.
- Logout clears the stored refresh token for the authenticated user.
- The frontend stores the access token and refresh token in `localStorage`.

## Session Policy

Spring Security is configured as stateless with CSRF disabled for the API flow.
Requests are authenticated by `JwtAuthenticationFilter`, which extracts the
Bearer token, validates it, looks up the user, and sets the security context for
the current request only.

## Follow-Up Notes

- For a production app, revisit frontend token storage and CSRF/XSS hardening.
- Keep auth schema changes in Flyway migrations.
- Keep protected route behavior aligned between backend security rules and the
  React auth context.
