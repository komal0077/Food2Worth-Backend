package com.example.FoodWaste.repository;
import com.example.FoodWaste.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email
    Optional<User> findByEmail(String email);

    // Find users by role
    List<User> findByRole(String role);
}