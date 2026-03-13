package com.ecommerce.user_service.security;

import com.ecommerce.user_service.repository.UserRepository;
import com.ecommerce.user_service.entity.Role;
import com.ecommerce.user_service.entity.User;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        // Default to USER if role is null in DB
        String roleName = (user.getRole() != null) ? user.getRole().name() : Role.USER.name();

        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(roleName)
                .build();
    }
}