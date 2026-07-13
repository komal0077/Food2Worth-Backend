package com.example.FoodWaste.controller;
import com.example.FoodWaste.dto.UserResponse;
import com.example.FoodWaste.security.AuthenticatedUser;
import com.example.FoodWaste.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Get All Users - admin only, exposes every account
    @GetMapping
    public List<UserResponse> getAllUsers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {

        requireAdmin(principal);

        return userService.getAllUsers(page, size).stream()
                .map(UserResponse::from)
                .toList();
    }

    // Get User By Id - self or admin
    @GetMapping("/{id}")
    public UserResponse getUserById(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {

        if (!principal.isSelfOrAdmin(id)) {
            throw new AccessDeniedException("You can only view your own profile");
        }

        return UserResponse.from(userService.getUserById(id));
    }

    // Get Users By Role - admin only
    @GetMapping("/role/{role}")
    public List<UserResponse> getUsersByRole(
            @PathVariable String role,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {

        requireAdmin(principal);

        return userService.getUsersByRole(role).stream()
                .map(UserResponse::from)
                .toList();
    }

    // Delete User - self or admin
    @DeleteMapping("/{id}")
    public String deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {

        if (!principal.isSelfOrAdmin(id)) {
            throw new AccessDeniedException("You can only delete your own account");
        }

        userService.deleteUser(id);

        return "User Deleted Successfully";
    }

    private void requireAdmin(AuthenticatedUser principal) {
        if (!principal.isAdmin()) {
            throw new AccessDeniedException("Admin access required");
        }
    }
}
