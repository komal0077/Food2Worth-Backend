package com.example.FoodWaste.controller;
import com.example.FoodWaste.dto.UserResponse;
import com.example.FoodWaste.entity.Role;
import com.example.FoodWaste.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Get All Users — admin only
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<UserResponse> getAllUsers(@PageableDefault(size = 20) Pageable pageable) {

        return userService.getAllUsers(pageable);
    }

    // Get User By Id — admin only; users fetch their own profile via /api/auth
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public UserResponse getUserById(@PathVariable Long id) {

        return userService.getUserById(id);
    }

    // Get Users By Role — admin only
    @GetMapping("/role/{role}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<UserResponse> getUsersByRole(
            @PathVariable Role role,
            @PageableDefault(size = 20) Pageable pageable) {

        return userService.getUsersByRole(role, pageable);
    }

    // Delete User — admin only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String deleteUser(@PathVariable Long id) {

        userService.deleteUser(id);

        return "User Deleted Successfully";
    }
}
