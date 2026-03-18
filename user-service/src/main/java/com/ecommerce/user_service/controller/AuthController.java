package com.ecommerce.user_service.controller;

import com.ecommerce.user_service.dto.LoginRequest;
import com.ecommerce.user_service.dto.RegisterRequest;
import com.ecommerce.user_service.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // USER REGISTRATION APIs
    @PostMapping("/register")
    public String register(@Valid @RequestBody RegisterRequest request) {

        authService.register(request);

        return "User registered successfully";
    }

    // USER LOGIN APIs
    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest request) {

        return authService.login(
                request.getUsername(),
                request.getPassword()
        );
    }

    // PUBLIC TEST ENDPOINT APIs
    @GetMapping("/test")
    public String test() {
        return "Auth endpoint working (public)";
    }
}