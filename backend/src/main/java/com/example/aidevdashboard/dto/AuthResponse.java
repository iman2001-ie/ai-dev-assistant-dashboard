package com.example.aidevdashboard.dto;

public class AuthResponse {
    private String token;
    private String username;
    private String refreshToken;

    public AuthResponse() {}
    public AuthResponse(String token, String username) { this.token = token; this.username = username; }
    public AuthResponse(String token, String username, String refreshToken) { this.token = token; this.username = username; this.refreshToken = refreshToken; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
