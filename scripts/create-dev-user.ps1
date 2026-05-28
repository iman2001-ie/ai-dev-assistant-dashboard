param(
    [string] $Username = "testuser",
    [string] $Email = "testuser@example.com",
    [string] $Password = "Password123!",
    [string] $ApiBaseUrl = "http://localhost:8080/api"
)

$ErrorActionPreference = "Stop"

$body = @{
    username = $Username
    email = $Email
    password = $Password
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod `
        -Method Post `
        -Uri "$ApiBaseUrl/auth/register" `
        -ContentType "application/json" `
        -Body $body

    Write-Output "Created local development user."
    Write-Output "Username: $($response.username)"
    Write-Output "Password: $Password"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 400) {
        Write-Output "User was not created. It may already exist."
        Write-Output "Try logging in with:"
        Write-Output "Username: $Username"
        Write-Output "Password: $Password"
        exit 0
    }

    Write-Output "Could not create local development user."
    Write-Output "Make sure the backend is running at $ApiBaseUrl."
    throw
}
