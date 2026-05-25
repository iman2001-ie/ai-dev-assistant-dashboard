$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$runtimeDir = Join-Path $root ".runtime"

function Stop-PidFile {
    param([string] $Path)

    if (Test-Path $Path) {
        $processId = [int](Get-Content $Path | Select-Object -First 1)
        if (Get-Process -Id $processId -ErrorAction SilentlyContinue) {
            Stop-Process -Id $processId -Force
        }
        Remove-Item $Path -Force
    }
}

function Stop-ListeningProcess {
    param([int] $Port)

    $connection = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($connection -and $connection.OwningProcess -gt 0) {
        Stop-Process -Id $connection.OwningProcess -Force
    }
}

Stop-PidFile -Path (Join-Path $runtimeDir "frontend.pid")
Stop-PidFile -Path (Join-Path $runtimeDir "backend.pid")
Stop-ListeningProcess -Port 5173
Stop-ListeningProcess -Port 8080

docker compose --project-directory $root down

Write-Output "Stopped local development stack."
