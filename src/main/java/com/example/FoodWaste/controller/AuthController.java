package com.example.FoodWaste.controller;

import com.example.FoodWaste.dto.AuthRequest;
import com.example.FoodWaste.entity.User;
import com.example.FoodWaste.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Register
    @PostMapping("/register")
    public User register(@RequestBody User user) {

        return authService.register(user);
    }

    // Login
    @PostMapping("/login")
    public String login(@RequestBody AuthRequest request) {

        return authService.login(request);
    }
}
