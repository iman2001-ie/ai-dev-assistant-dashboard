param(
    [switch] $Force
)

$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")

if (-not $Force) {
    $confirmation = Read-Host "This will delete the local Docker PostgreSQL volume for this project. Type RESET to continue"
    if ($confirmation -ne "RESET") {
        Write-Output "Database reset cancelled."
        exit 0
    }
}

docker compose --project-directory $root down -v
docker compose --project-directory $root up -d

Write-Output "Local PostgreSQL data was reset."
Write-Output "Start the backend so Flyway can recreate the schema:"
Write-Output ".\scripts\start-backend.ps1"
