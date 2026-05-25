$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")

function Get-PortStatus {
    param([int] $Port)

    $connection = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($connection) {
        return "listening (PID $($connection.OwningProcess))"
    }
    return "not listening"
}

Write-Output "Frontend 5173: $(Get-PortStatus -Port 5173)"
Write-Output "Backend  8080: $(Get-PortStatus -Port 8080)"
Write-Output "Postgres Docker:"
docker compose --project-directory $root ps
