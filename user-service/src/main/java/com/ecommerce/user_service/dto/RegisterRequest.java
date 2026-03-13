package com.ecommerce.user_service.dto;

import com.ecommerce.user_service.entity.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/*
 DTO used to receive user registration data
 Validation ensures bad data is rejected
*/

public class RegisterRequest {

    @NotBlank(message = "Username cannot be empty")
    private String username;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email cannot be empty")
    private String email;

    @Size(min = 4, message = "Password must be at least 4 characters")
    private String password;

    // user can send role for now
    private Role role;

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }
}