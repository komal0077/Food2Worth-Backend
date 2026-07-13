package com.example.FoodWaste.controller;

import com.example.FoodWaste.dto.AuthRequest;
import com.example.FoodWaste.dto.AuthResponse;
import com.example.FoodWaste.dto.RegisterRequest;
import com.example.FoodWaste.dto.UserResponse;
import com.example.FoodWaste.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Register
    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {

        return authService.register(request);
    }

    // Login
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {

        return authService.login(request);
    }
}
