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

The app currently supports normal registered users. It does not have roles or a real admin/super-user model yet.

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

To create a different local user:

```powershell
.\scripts\create-dev-user.ps1 -Username demo -Email demo@example.com -Password "Password123!"
```

## Reset Local Development Database

Only use this for local Docker development data. It deletes the project Docker PostgreSQL volume and starts a fresh database container:

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
