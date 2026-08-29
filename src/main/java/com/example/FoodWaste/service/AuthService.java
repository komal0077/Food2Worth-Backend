package com.example.FoodWaste.service;

import com.example.FoodWaste.dto.AuthRequest;
import com.example.FoodWaste.dto.AuthResponse;
import com.example.FoodWaste.dto.RegisterRequest;
import com.example.FoodWaste.dto.UserResponse;
import com.example.FoodWaste.entity.Role;
import com.example.FoodWaste.entity.User;
import com.example.FoodWaste.repository.UserRepository;
import com.example.FoodWaste.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Set<Role> SELF_REGISTERABLE_ROLES =
            Set.of(Role.DONOR, Role.NGO, Role.VOLUNTEER);

    private final UserRepository userRepository;

    private final JwtUtil jwtUtil;

    private final PasswordEncoder passwordEncoder;

    // Register User
    public UserResponse register(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already registered");
        }

        Role role = parseRole(request.getRole());

        if (!SELF_REGISTERABLE_ROLES.contains(role)) {
            throw new IllegalArgumentException(
                    "Role must be one of " + SELF_REGISTERABLE_ROLES);
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(role)
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .isVerified(false)
                .isApproved(false)
                .createdAt(LocalDateTime.now())
                .build();

        User saved = userRepository.save(user);

        log.info("New user registered: id={}, role={}", saved.getId(), saved.getRole());

        return toResponse(saved);
    }

    private Role parseRole(String role) {

        if (role == null) {
            throw new IllegalArgumentException(
                    "Role must be one of " + SELF_REGISTERABLE_ROLES);
        }

        try {
            return Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Role must be one of " + SELF_REGISTERABLE_ROLES);
        }
    }

    // Login User
    public AuthResponse login(AuthRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login attempt for unknown email");
                    return new BadCredentialsException("Invalid email or password");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {

            log.warn("Failed login attempt: userId={}", user.getId());

            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        log.info("User logged in: id={}", user.getId());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    static UserResponse toResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .address(user.getAddress())
                .latitude(user.getLatitude())
                .longitude(user.getLongitude())
                .profilePhoto(user.getProfilePhoto())
                .isVerified(user.getIsVerified())
                .isApproved(user.getIsApproved())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
