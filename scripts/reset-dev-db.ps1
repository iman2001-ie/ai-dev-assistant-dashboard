param(
    [switch] $Force
)

$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$stopScript = Join-Path $root "scripts\stop-dev.ps1"
$envPath = Join-Path $root ".env.local"

if (-not $Force) {
    $confirmation = Read-Host "This will delete the local Docker PostgreSQL volume for this project. Type RESET to continue"
    if ($confirmation -ne "RESET") {
        Write-Output "Database reset cancelled."
        exit 0
    }
}

if (Test-Path $stopScript) {
    & $stopScript
}

docker compose --project-directory $root down -v
docker compose --project-directory $root up -d

Write-Output "Local PostgreSQL data was reset."
if (Test-Path $envPath) {
    $databaseUrlLine = Get-Content $envPath | Where-Object { $_.Trim().StartsWith("DATABASE_URL=") } | Select-Object -First 1
    if ($databaseUrlLine -and $databaseUrlLine -notlike "*localhost:5432/ai_dev_dashboard*") {
        Write-Warning ".env.local has a custom DATABASE_URL. This reset only clears the Docker PostgreSQL database from docker-compose.yml."
    }
}
Write-Output "Start the backend so Flyway can recreate the schema:"
Write-Output ".\scripts\start-backend.ps1"
