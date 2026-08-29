package com.example.FoodWaste.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// What a client is allowed to submit at registration time.
// Verification/approval flags are deliberately absent here —
// they must never be set by the caller (see AuthService.register).
// "role" is self-declared (DONOR/NGO/VOLUNTEER) and validated server-side;
// ADMIN can never be granted through this endpoint.
@Data
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private String phone;

    @NotBlank(message = "Role is required")
    private String role;

    private String address;

    private Double latitude;

    private Double longitude;
}
