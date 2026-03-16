    package com.ecommerce.user_service.repository;


    import com.ecommerce.user_service.entity.User;
    import org.springframework.data.jpa.repository.JpaRepository;

    import java.util.Optional;

    public interface UserRepository extends JpaRepository<User, Long> {

        // Custom query method to find a user used during login to verify user
        Optional<User> findByUsername(String username);

        Optional<User> findByEmail(String email);
    }