# Run project tests and checks
# Usage: .\scripts\run-tests.ps1

$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "== Running backend tests (Maven) =="
Push-Location (Join-Path $root '..' )
# ensure we are at repo root
Set-Location -Path (Resolve-Path "$root\..")
Set-Location -Path "$PWD\backend"
try {
    & mvn test
    $backendExit = $LASTEXITCODE
} catch {
    Write-Host "Backend tests failed to start: $_"
    $backendExit = 1
}
Pop-Location

Write-Host "== Running frontend lint and TypeScript build =="
Push-Location (Join-Path $root '..' )
Set-Location -Path "$PWD\frontend"
try {
    npm run lint
    $lintExit = $LASTEXITCODE
} catch {
    Write-Host "Lint failed to start: $_"
    $lintExit = 1
}
try {
    npx tsc -b
    $tsExit = $LASTEXITCODE
} catch {
    Write-Host "TypeScript build failed to start: $_"
    $tsExit = 1
}
Pop-Location

Write-Host "\n== Summary =="
if ($backendExit -eq 0 -and $lintExit -eq 0 -and $tsExit -eq 0) {
    Write-Host "All checks passed"
    exit 0
} else {
    Write-Host "One or more checks failed (backend: $backendExit, lint: $lintExit, tsc: $tsExit)"
    exit 1
}
