package com.example.FoodWaste.service;
import com.example.FoodWaste.dto.UserResponse;
import com.example.FoodWaste.entity.Role;
import com.example.FoodWaste.entity.User;
import com.example.FoodWaste.exception.ResourceNotFoundException;
import com.example.FoodWaste.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // Get All Users
    public Page<UserResponse> getAllUsers(Pageable pageable) {

        return userRepository.findAll(pageable)
                .map(AuthService::toResponse);
    }

    // Get User By Id
    public UserResponse getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

        return AuthService.toResponse(user);
    }

    // Get Users By Role
    public Page<UserResponse> getUsersByRole(Role role, Pageable pageable) {

        return userRepository.findByRole(role, pageable)
                .map(AuthService::toResponse);
    }

    // Delete User
    public void deleteUser(Long id) {

        userRepository.deleteById(id);
    }
}
