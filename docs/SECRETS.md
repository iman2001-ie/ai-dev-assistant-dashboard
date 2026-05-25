# Secrets and API Keys

This project should never store real secrets in Git.

## OpenAI API Key

The backend reads the OpenAI key from this environment variable:

```bash
OPENAI_API_KEY
```

The key belongs on the backend only. Do not put it in React code, browser code, or any variable that starts with `VITE_`.

Why: frontend code is shipped to the browser. Anything exposed to the browser can be seen by users.

## Local Development

Create a private local env file from the example:

```bash
cp .env.example .env.local
```

Then set your real key in `.env.local`:

```bash
OPENAI_API_KEY=your_real_key_here
```

The `.env.local` file is ignored by Git.

Start the backend normally:

```powershell
cd backend
mvn spring-boot:run
```

Spring Boot imports `.env.local` automatically when the file exists in the project root.

The full local stack script also loads `.env.local` through the backend startup flow:

```powershell
.\scripts\start-dev.ps1
```

You can also start the backend with the local helper script:

```powershell
.\scripts\start-backend.ps1
```

That script loads `.env.local` into the backend process environment and then runs `mvn spring-boot:run`. It is mostly useful if your terminal or tooling does not pick up the config import as expected.

On Windows, the script also tells Java to use the Windows root certificate store. This can help when a browser trusts a local or corporate certificate but the JDK does not.

To disable that behavior and use Java's default trust store:

```powershell
.\scripts\start-backend.ps1 -UseJavaDefaultTrustStore
```

PowerShell can also set the key manually for the current terminal session:

```powershell
$env:OPENAI_API_KEY="your_real_key_here"
```

Then start the backend from the same terminal:

```powershell
cd backend
mvn spring-boot:run
```

This avoids writing the key to any project file.

## Production

In production, store secrets in the deployment platform's environment variable or secret manager settings.

Examples:

- Render: Environment variables
- Railway: Variables
- Fly.io: Secrets
- Docker Compose on a private server: environment variables or an uncommitted `.env` file
- AWS/GCP/Azure: managed secret storage

Production rules:

- Never commit real API keys.
- Never expose the OpenAI key to frontend/browser code.
- Use separate keys for development, staging, and production when possible.
- Set usage limits and monitor usage in the OpenAI dashboard.
- Rotate the key immediately if it is ever committed or shared accidentally.

## Current App Behavior

If `OPENAI_API_KEY` is empty or missing, the backend returns a mock assistant response. That means the app is still usable without a real key.
