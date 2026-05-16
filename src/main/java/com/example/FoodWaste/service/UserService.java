package com.example.FoodWaste.service;
import com.example.FoodWaste.entity.User;
import com.example.FoodWaste.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // Register User
    public User registerUser(User user) {

        user.setCreatedAt(LocalDateTime.now());

        user.setIsApproved(true);

        user.setIsVerified(true);

        return userRepository.save(user);
    }

    // Get All Users
    public List<User> getAllUsers() {

        return userRepository.findAll();
    }

    // Get User By Id
    public User getUserById(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User Not Found"));
    }

    // Get Users By Role
    public List<User> getUsersByRole(String role) {

        return userRepository.findByRole(role);
    }

    // Delete User
    public void deleteUser(Long id) {

        userRepository.deleteById(id);
    }
}
