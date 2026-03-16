package com.ecommerce.user_service.service;

import com.ecommerce.user_service.dto.RegisterRequest;
import com.ecommerce.user_service.entity.Role;
import com.ecommerce.user_service.entity.User;
import com.ecommerce.user_service.repository.UserRepository;
import com.ecommerce.user_service.security.JwtService;

import com.ecommerce.user_service.exception.UserNotFoundException;
import com.ecommerce.user_service.exception.InvalidPasswordException;
import com.ecommerce.user_service.exception.DuplicateUserException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // USER REGISTRATION
    public void register(RegisterRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new DuplicateUserException("USER_ALREADY_EXISTS");
        }

        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateUserException("EMAIL_ALREADY_EXISTS");
        }

        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // allow role from request for now
        user.setRole(request.getRole() != null ? request.getRole() : Role.USER);

        userRepository.save(user);
    }

    // USER LOGIN
    public String login(String username, String password) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("USER_NOT_FOUND"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidPasswordException("INVALID_PASSWORD");
        }

        return jwtService.generateToken(user.getUsername());
    }
}