# AI Dev Assistant Dashboard

A beginner-friendly full-stack developer productivity dashboard for managing coding tasks, saving error logs, and asking an AI assistant for help. The project is intentionally small, but it uses a structure that can grow into a larger app.

## Tech Stack

- Frontend: React, TypeScript, Vite, React Router
- Backend: Java, Spring Boot, Spring Data JPA, Flyway
- Database: PostgreSQL
- API style: REST
- AI: OpenAI API when `OPENAI_API_KEY` is provided, mock responses otherwise
- Styling: plain CSS

## Project Structure

```text
ai-dev-assistant-dashboard/
  frontend/        React + TypeScript app
  backend/         Spring Boot REST API
  docker-compose.yml
  README.md
```

## Run PostgreSQL

Docker is optional. Use either Docker or your locally installed PostgreSQL.

### Option A: Local PostgreSQL and pgAdmin

If you installed PostgreSQL locally, create the database and user manually.

In pgAdmin:

1. Open pgAdmin.
2. Connect to your local PostgreSQL server.
3. Right-click `Databases`.
4. Select `Create` then `Database`.
5. Name it `ai_dev_dashboard`.
6. Open the Query Tool and run:

```sql
CREATE USER dashboard_user WITH PASSWORD 'dashboard_password';
CREATE DATABASE ai_dev_dashboard OWNER dashboard_user;
GRANT ALL PRIVILEGES ON DATABASE ai_dev_dashboard TO dashboard_user;
```

If permission errors happen when the app starts, also run this while connected to the `ai_dev_dashboard` database:

```sql
GRANT ALL ON SCHEMA public TO dashboard_user;
GRANT CREATE ON SCHEMA public TO dashboard_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO dashboard_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO dashboard_user;
```

The same starter SQL is available at `backend/src/main/resources/db/setup-local-postgres.sql`.

### Option B: Docker

```bash
docker compose up -d
```

If your Docker installation uses the standalone Compose command, run:

```bash
docker-compose up -d
```

PostgreSQL should be available at `localhost:5432`.

Default local credentials:

- Database: `ai_dev_dashboard`
- Username: `dashboard_user`
- Password: `dashboard_password`

## Run Backend

From `backend/`:

```bash
mvn spring-boot:run
```

The API runs at `http://localhost:8080`.

Useful environment variables:

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/ai_dev_dashboard
DATABASE_USERNAME=dashboard_user
DATABASE_PASSWORD=dashboard_password
OPENAI_API_KEY=
OPENAI_MODEL=gpt-4o-mini
FRONTEND_ORIGIN=http://localhost:5173
```

If `OPENAI_API_KEY` is blank or missing, the backend returns a helpful mock AI response.

## Run Frontend

From `frontend/`:

```bash
npm install
npm run dev
```

If npm cannot write to your global cache directory, keep the cache inside this project:

```bash
npm install --cache .npm-cache
```

The frontend runs at `http://localhost:5173`.

Optional frontend environment variable:

```bash
VITE_API_BASE_URL=http://localhost:8080/api
```

## API Endpoints

### Tasks

- `GET /api/tasks`
- `GET /api/tasks/{id}`
- `POST /api/tasks`
- `PUT /api/tasks/{id}`
- `DELETE /api/tasks/{id}`

### Logs

- `GET /api/logs`
- `GET /api/logs/{id}`
- `POST /api/logs`
- `PUT /api/logs/{id}`
- `DELETE /api/logs/{id}`
- `GET /api/logs/unresolved`

### AI Assistant

- `POST /api/chat`
- `GET /api/chat/history`

### Dashboard

- `GET /api/dashboard/summary`

## Example Chat Request

```json
{
  "message": "Can you help me understand this error?",
  "errorLogId": 1
}
```

## Future Improvements

- Add authentication and per-user data
- Add task due dates and tags
- Add conversation grouping
- Add streaming AI responses
- Add tests for services and controllers
- Add production Dockerfiles for backend and frontend
- Add richer agent tools and explicit tool-call traces
