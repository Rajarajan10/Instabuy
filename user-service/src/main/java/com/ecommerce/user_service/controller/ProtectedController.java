package com.ecommerce.user_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProtectedController {

    // This endpoint is PROTECTED — requires a valid JWT Bearer token
    @GetMapping("/test")
    public String protectedTest() {
        return "Protected API is working! Your JWT token is valid.";
    }
}

