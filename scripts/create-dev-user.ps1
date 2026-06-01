param(
    [string] $Username = "testuser",
    [string] $Email = "testuser@example.com",
    [string] $Password = "Password123!",
    [string] $ApiBaseUrl = "http://localhost:8080/api",
    [switch] $SkipSeedDataAssignment
)

$ErrorActionPreference = "Stop"

$body = @{
    username = $Username
    email = $Email
    password = $Password
} | ConvertTo-Json

function Read-ErrorMessage {
    param($Response)

    if (-not $Response) {
        return $null
    }

    try {
        $stream = $Response.GetResponseStream()
        if (-not $stream) {
            return $null
        }

        $reader = New-Object System.IO.StreamReader($stream)
        try {
            $raw = $reader.ReadToEnd()
            if (-not $raw) {
                return $null
            }

            try {
                $parsed = $raw | ConvertFrom-Json -ErrorAction Stop
                if ($parsed.message) {
                    return $parsed.message
                }
            } catch {
                return $raw
            }

            return $raw
        } finally {
            $reader.Dispose()
            $stream.Dispose()
        }
    } catch {
        return $null
    }
}

function Assign-UnownedSeedData {
    param(
        [string] $TargetUsername,
        [string] $Token
    )

    if ($SkipSeedDataAssignment) {
        return
    }

    if (-not $Token) {
        Write-Output "Skipped seed data assignment because no login token is available."
        return
    }

    try {
        $response = Invoke-RestMethod `
            -Method Post `
            -Uri "$ApiBaseUrl/dev/claim-seed-data" `
            -Headers @{ Authorization = "Bearer $Token" }

        Write-Output "Assigned unowned seed data to $TargetUsername."
        Write-Output "Tasks assigned: $($response.tasksAssigned)"
        Write-Output "Logs assigned: $($response.logsAssigned)"
        Write-Output "Chat messages assigned: $($response.chatMessagesAssigned)"
    } catch {
        Write-Output "Could not assign seed data through the backend."
        Write-Output "Make sure the backend includes /api/dev/claim-seed-data and is running at $ApiBaseUrl."
    }
}

function Register-User {
    return Invoke-RestMethod `
        -Method Post `
        -Uri "$ApiBaseUrl/auth/register" `
        -ContentType "application/json" `
        -Body $body
}

function Login-User {
    return Invoke-RestMethod `
        -Method Post `
        -Uri "$ApiBaseUrl/auth/login" `
        -ContentType "application/json" `
        -Body $body
}

try {
    $response = Register-User

    Write-Output "Created local development user."
    Write-Output "Username: $($response.username)"
    Write-Output "Password: $Password"
    Assign-UnownedSeedData -TargetUsername $response.username -Token $response.token
} catch {
    $response = $_.Exception.Response
    $statusCode = if ($response) { [int]$response.StatusCode } else { $null }
    $errorMessage = Read-ErrorMessage -Response $response

    if (-not $response) {
        Write-Output "Could not reach the backend at $ApiBaseUrl."
        Write-Output "Start the backend first with:"
        Write-Output ".\scripts\start-backend.ps1"
        exit 1
    }

    if ($statusCode -eq 400) {
        Write-Output "User was not created."
        if ($errorMessage) {
            Write-Output "Backend message: $errorMessage"
        }
        try {
            $loginResponse = Login-User
            Assign-UnownedSeedData -TargetUsername $loginResponse.username -Token $loginResponse.token
        } catch {
            Write-Output "Could not log in as $Username to assign seed data."
        }
        Write-Output "If you want a fresh account, try a different username/email pair."
        Write-Output "Example:"
        Write-Output ".\scripts\create-dev-user.ps1 -Username freshuser -Email freshuser@example.com"
        exit 0
    }

    Write-Output "Could not create local development user."
    if ($statusCode) {
        Write-Output "HTTP status: $statusCode"
    }
    if ($errorMessage) {
        Write-Output "Backend message: $errorMessage"
    }
    Write-Output "Make sure the backend is running at $ApiBaseUrl."
    exit 1
}
