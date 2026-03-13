package com.ecommerce.user_service.dto;


// DTO used to send JWT token back to the client after successful login
public class AuthResponse {

    // JWT token generated after auth
    private String token;


    public AuthResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}