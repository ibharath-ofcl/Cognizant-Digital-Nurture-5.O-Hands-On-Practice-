package com.cognizant.jwtauthentication.dto;

/**
 * Data Transfer Object (DTO) for login responses containing the generated token.
 */
public class LoginResponse {

    private String token;
    private String type = "Bearer";

    public LoginResponse() {
    }

    public LoginResponse(String token) {
        this.token = token;
    }

    public LoginResponse(String token, String type) {
        this.token = token;
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
