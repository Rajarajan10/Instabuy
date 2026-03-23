package com.ecommerce.user_service.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.user_service.dto.UpdateUserRequest;
import com.ecommerce.user_service.dto.UserResponse;
import com.ecommerce.user_service.entity.User;
import com.ecommerce.user_service.repository.UserRepository;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //GET CURRENT USER
    @GetMapping("/me")
    public UserResponse getCurrentUser(
            @RequestHeader("X-User-Name") String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }

    // GET ALL USERS
    @GetMapping("/all")
    public List<UserResponse> getAllUsers(
            @RequestHeader("X-User-Role") String role) {

        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("Access denied");
        }

        return userRepository.findAll().stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole()
                ))
                .collect(Collectors.toList());
    }

    // DELETE USER (ADMIN)
    @DeleteMapping("/{id}")
    public String deleteUser(
            @PathVariable Long id,
            @RequestHeader("X-User-Role") String role) {

        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("Access denied");
        }

        userRepository.deleteById(id);
        return "User deleted successfully";
    }

    // UPDATE USER (SELF ONLY)
    @PutMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request,
            @RequestHeader("X-User-Name") String username
    ) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // check ownership
        if (!user.getUsername().equals(username)) {
            throw new RuntimeException("You are not allowed to update this user");
        }

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(user);

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }
}