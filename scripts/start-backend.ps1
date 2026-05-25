param(
    [string] $EnvFile = ".env.local",
    [switch] $UseJavaDefaultTrustStore
)

$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$envPath = Join-Path $root $EnvFile

if (Test-Path $envPath) {
    Get-Content $envPath | ForEach-Object {
        $line = $_.Trim()
        if ($line -eq "" -or $line.StartsWith("#")) {
            return
        }

        $separatorIndex = $line.IndexOf("=")
        if ($separatorIndex -lt 1) {
            return
        }

        $name = $line.Substring(0, $separatorIndex).Trim()
        $value = $line.Substring($separatorIndex + 1).Trim().Trim('"').Trim("'")
        Set-Item -Path "Env:$name" -Value $value
    }
}

$isWindowsHost = $env:OS -eq "Windows_NT" -or (Get-Variable -Name IsWindows -ErrorAction SilentlyContinue).Value

if (-not $UseJavaDefaultTrustStore -and $isWindowsHost) {
    $windowsTrustStoreOption = "-Djavax.net.ssl.trustStoreType=Windows-ROOT"
    if ([string]::IsNullOrWhiteSpace($env:JAVA_TOOL_OPTIONS)) {
        $env:JAVA_TOOL_OPTIONS = $windowsTrustStoreOption
    } elseif ($env:JAVA_TOOL_OPTIONS -notlike "*trustStoreType*") {
        $env:JAVA_TOOL_OPTIONS = "$env:JAVA_TOOL_OPTIONS $windowsTrustStoreOption"
    }
}

Push-Location (Join-Path $root "backend")
try {
    mvn spring-boot:run
} finally {
    Pop-Location
}
