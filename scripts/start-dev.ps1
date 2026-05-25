param(
    [string] $EnvFile = ".env.local",
    [switch] $SkipInstall
)

$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$runtimeDir = Join-Path $root ".runtime"
$frontendDir = Join-Path $root "frontend"
$backendScript = Join-Path $root "scripts\start-backend.ps1"
$backendLog = Join-Path $runtimeDir "backend.log"
$backendErr = Join-Path $runtimeDir "backend.err.log"
$frontendLog = Join-Path $runtimeDir "frontend.log"
$frontendErr = Join-Path $runtimeDir "frontend.err.log"

New-Item -ItemType Directory -Force $runtimeDir | Out-Null

function Stop-ListeningProcess {
    param([int] $Port)

    $connection = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($connection -and $connection.OwningProcess -gt 0) {
        Stop-Process -Id $connection.OwningProcess -Force
    }
}

docker compose --project-directory $root up -d

if (-not $SkipInstall -and -not (Test-Path (Join-Path $frontendDir "node_modules"))) {
    Push-Location $frontendDir
    try {
        npm install
    } finally {
        Pop-Location
    }
}

Stop-ListeningProcess -Port 8080
Stop-ListeningProcess -Port 5173

$backendProcess = Start-Process -FilePath powershell.exe `
    -ArgumentList @("-ExecutionPolicy", "Bypass", "-File", $backendScript, "-EnvFile", $EnvFile) `
    -WorkingDirectory $root `
    -WindowStyle Hidden `
    -RedirectStandardOutput $backendLog `
    -RedirectStandardError $backendErr `
    -PassThru

$frontendProcess = Start-Process -FilePath npm.cmd `
    -ArgumentList @("run", "dev", "--", "--host", "127.0.0.1") `
    -WorkingDirectory $frontendDir `
    -WindowStyle Hidden `
    -RedirectStandardOutput $frontendLog `
    -RedirectStandardError $frontendErr `
    -PassThru

Set-Content -Path (Join-Path $runtimeDir "backend.pid") -Value $backendProcess.Id
Set-Content -Path (Join-Path $runtimeDir "frontend.pid") -Value $frontendProcess.Id

Write-Output "Started local development stack."
Write-Output "Frontend: http://127.0.0.1:5173"
Write-Output "Backend:  http://localhost:8080"
Write-Output "Logs:     .runtime/"
