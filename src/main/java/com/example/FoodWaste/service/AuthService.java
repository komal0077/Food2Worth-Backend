package com.example.FoodWaste.service;

import com.example.FoodWaste.dto.AuthRequest;
import com.example.FoodWaste.dto.AuthResponse;
import com.example.FoodWaste.dto.RegisterRequest;
import com.example.FoodWaste.dto.UserResponse;
import com.example.FoodWaste.entity.User;
import com.example.FoodWaste.repository.UserRepository;
import com.example.FoodWaste.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    // Register User
    public UserResponse register(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole())
                .isVerified(true)
                .isApproved(true)
                .createdAt(LocalDateTime.now())
                .build();

        return UserResponse.from(userRepository.save(user));
    }

    // Login User
    public AuthResponse login(AuthRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
