# Local Development Notes

These notes are for contributors working on the project locally. The public README keeps the main project overview shorter.

## Local Services

The app expects:

- Frontend: `http://localhost:5173`
- Frontend alternate: `http://127.0.0.1:5173`
- Backend: `http://localhost:8080`
- PostgreSQL: `localhost:5432`

## Start and Stop the App

Recommended local workflow from the project root:

```powershell
.\scripts\start-dev.ps1
```

This starts:

- PostgreSQL with Docker Compose
- Spring Boot backend on `http://localhost:8080`
- Vite frontend on `http://127.0.0.1:5173`

It writes local runtime logs and PID files to:

```text
.runtime/
```

That folder is ignored by Git.

Stop everything:

```powershell
.\scripts\stop-dev.ps1
```

Check status:

```powershell
.\scripts\status-dev.ps1
```

Skip frontend dependency installation if `node_modules` already exists:

```powershell
.\scripts\start-dev.ps1 -SkipInstall
```

## Environment Variables

Backend:

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/ai_dev_dashboard
DATABASE_USERNAME=dashboard_user
DATABASE_PASSWORD=dashboard_password
OPENAI_API_KEY=
OPENAI_MODEL=gpt-4o-mini
FRONTEND_ORIGIN=http://localhost:5173
```

Frontend:

```bash
VITE_API_BASE_URL=/api
```

The frontend dev server proxies `/api` to the backend by default, so you usually do not need to set `VITE_API_BASE_URL` locally. Use `http://localhost:8080/api` only if you intentionally want to bypass the proxy.

Do not store real API keys in committed files. See [Secrets and API keys](SECRETS.md).

## PostgreSQL Options

### Recommended: Docker

```bash
docker compose up -d
```

Default local credentials:

- Database: `ai_dev_dashboard`
- Username: `dashboard_user`
- Password: `dashboard_password`

Using Docker is the preferred development path because it keeps the database version and credentials aligned with the project.

### Alternative: Local PostgreSQL

Use a local PostgreSQL installation only if you intentionally do not want Docker for this project. The application code does not care which option you use as long as the database is reachable at the configured `DATABASE_URL`.

Create the database manually:

```sql
CREATE USER dashboard_user WITH PASSWORD 'dashboard_password';
CREATE DATABASE ai_dev_dashboard OWNER dashboard_user;
GRANT ALL PRIVILEGES ON DATABASE ai_dev_dashboard TO dashboard_user;
```

If permission errors happen during startup, run this while connected to the `ai_dev_dashboard` database:

```sql
GRANT ALL ON SCHEMA public TO dashboard_user;
GRANT CREATE ON SCHEMA public TO dashboard_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO dashboard_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO dashboard_user;
```

The same starter SQL is available at:

```text
backend/src/main/resources/db/setup-local-postgres.sql
```

## Local Auth Users

The app supports normal registered users. It does not have roles or a real admin/super-user model yet.

Users can also register through the frontend. Login uses the username, not the email address.

To create a local development user after the backend is running:

```powershell
.\scripts\create-dev-user.ps1
```

Default local credentials:

```text
Username: testuser
Password: Password123!
Email: testuser@example.com
```

Login uses the username, not the email address.

The script also assigns any unowned sample tasks, logs, and chat messages to that user. This keeps the seeded demo data visible without making it shared across every account.

To create a different local user:

```powershell
.\scripts\create-dev-user.ps1 -Username demo -Email demo@example.com -Password "Password123!"
```

If you want to do the whole flow manually after a clean reset, use this order:

1. Reset the local database:

   ```powershell
   .\scripts\reset-dev-db.ps1
   ```

2. Start the backend and wait until Spring Boot reports that it started:

   ```powershell
   .\scripts\start-backend.ps1
   ```

3. Create the local user:

   ```powershell
   .\scripts\create-dev-user.ps1
   ```

4. Start the frontend in a separate terminal if it is not already running:

   ```powershell
   cd frontend
   npm run dev
   ```

If `testuser` already exists from a previous run, create a fresh one with a different username/email instead of waiting for the script to retry:

```powershell
.\scripts\create-dev-user.ps1 -Username freshuser -Email freshuser@example.com
```

If you want to create the user without claiming unowned sample data:

```powershell
.\scripts\create-dev-user.ps1 -SkipSeedDataAssignment
```

## Account Management

Authenticated users can open the account page from the username shown in the sidebar.

Current account behavior:

- Username and email can be updated from the account page.
- Password changes require the current password and a new password.
- The current password is never loaded into the profile form.
- Account deletion requires a confirmation message.
- Deleting an account removes that user's local tasks, logs, and chat history.

This is intentionally simple local-MVP account management. There is no forgot-password email flow yet; the frontend includes a placeholder forgot-password page for future work.

## Reset Local Development Database

Only use this for local Docker development data. It stops the local app, deletes the project Docker PostgreSQL volume, and starts a fresh database container:

```powershell
.\scripts\reset-dev-db.ps1
```

Use the force flag only when you intentionally want a non-interactive reset:

```powershell
.\scripts\reset-dev-db.ps1 -Force
```

After resetting, start the backend so Flyway can recreate the schema, then create a local development user:

```powershell
.\scripts\start-backend.ps1
.\scripts\create-dev-user.ps1
```

If an old account still logs in after a reset, check two things:

1. Make sure the backend was restarted after the reset. Old browser tokens may still show a username until the frontend makes a fresh API request.
2. Check `.env.local` for a custom `DATABASE_URL`. The reset script only clears the Docker database from `docker-compose.yml`.

If the UI still behaves as if an old user is signed in, use the Logout button or clear browser local storage for `127.0.0.1:5173` / `localhost:5173`, then log in again.

## Useful Commands

Use these if you prefer to run services manually instead of `start-dev.ps1`.

Frontend:

```bash
cd frontend
npm install
npm run dev
npm run lint
npx tsc -b
```

Backend:

```bash
cd backend
mvn spring-boot:run
mvn test
```

The backend automatically imports `.env.local` from the project root when it exists. To explicitly load private values from `.env.local` before starting the backend:

```powershell
.\scripts\start-backend.ps1
```

On Windows, the script also uses the Windows root certificate store for Java HTTPS requests. That can help if outbound API calls fail with `PKIX path building failed`.

If npm cannot write to the global cache directory, keep the cache inside the project:

```bash
npm install --cache .npm-cache
```

## Flyway

Database migrations live in:

```text
backend/src/main/resources/db/migration
```

Flyway runs automatically when the backend starts.

## Generated Files

The following paths are local/generated and are ignored by Git:

- `.runtime/`
- `backend/target/`
- `frontend/node_modules/`
- `frontend/dist/`
- `frontend/tsconfig.tsbuildinfo`
- `*.log`

They can be deleted when you want a clean local workspace. Do not delete `.env.local` unless you intentionally want to remove private local configuration.

## Refresh README Screenshots

With the frontend and backend running, refresh the README screenshots with:

```powershell
node scripts\capture-readme-screenshots.mjs
```

The script logs in as `testuser` by default, captures the main app pages at a fixed desktop viewport, and writes images to `docs/screenshots/`. It uses the `React state update warning` error log as the AI Assistant context when available.

## Refresh README Demo

The README walkthrough is generated from the live local app. Before running it, make sure:

- PostgreSQL, the backend, and the frontend are running.
- `testuser` can log in with the default local password.
- FFmpeg is installed and available on `PATH`.

Then run:

```powershell
node scripts\capture-readme-demo-video.mjs
```

The script opens a Chromium-based browser, records the app with FFmpeg, logs in as `testuser`, creates fresh `Demo:` task and error-log records, asks the assistant about the new log, previews the account page, and writes:

```text
docs/demo/walkthrough.mp4
docs/demo/walkthrough.gif
```

The script deletes old `Demo:` tasks and logs for `testuser` before recording so the walkthrough stays repeatable.

By default, the script reuses this ignored browser profile:

```text
.runtime/readme-demo-browser-profile/
```

Reusing the profile prevents Edge or Chrome first-run popups from appearing in every recording. The script still clears the app's local/session storage before recording so the login flow starts cleanly.

To slow the demo down, set `DEMO_SPEED` before running the script. For example, `1.35` makes the walkthrough about 35% slower:

```powershell
$env:DEMO_SPEED="1.35"
node scripts\capture-readme-demo-video.mjs
Remove-Item Env:\DEMO_SPEED
```
