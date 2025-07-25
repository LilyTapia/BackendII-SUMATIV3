package com.letrasypapeles.backend.dto;

public class LoginResponse {

    private String token;
    private String type = "Bearer";

    public LoginResponse() {
    }

    public LoginResponse(String token) {
        this.token = token;
        this.type = "Bearer";
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
