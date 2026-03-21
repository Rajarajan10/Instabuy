package com.ecommerce.user_service.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.user_service.dto.UpdateUserRequest;
import com.ecommerce.user_service.dto.UserResponse;
import com.ecommerce.user_service.entity.User;
import com.ecommerce.user_service.repository.UserRepository;

@RestController
@RequestMapping("/users")
public class UserController {

    //Dependency Injection
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // GET CURRENT LOGGED USER
    @GetMapping("/me")
    public UserResponse getCurrentUser(Authentication authentication) {

        // get username from JWT
        String username = authentication.getName();

        // find user in database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // convert to response DTO
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }

    // 2️⃣ GET ALL USERS (ADMIN)
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {

        List<User> users = userRepository.findAll();

        return users.stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole()
                ))
                .collect(Collectors.toList());
    }

    // DELETE USER (ADMIN )

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(@PathVariable Long id) {

        userRepository.deleteById(id);

        return "User deleted successfully";
    }


    //  UPDATE USER
    @PutMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request,
            Authentication authentication
    ) {

        // get logged-in username from JWT
        String loggedInUsername = authentication.getName();

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // check if logged-in user is same as target user
        if (!user.getUsername().equals(loggedInUsername)) {
            throw new RuntimeException("You are not allowed to update this user");
        }

        // update email
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        // update password
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