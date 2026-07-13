package com.example.FoodWaste.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class AuthenticatedUser extends User {

    private final Long id;

    private final String fullName;

    public AuthenticatedUser(
            Long id,
            String fullName,
            String email,
            String password,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(email, password, authorities);
        this.id = id;
        this.fullName = fullName;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return getUsername();
    }

    public boolean isAdmin() {
        return getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));
    }

    public boolean isSelfOrAdmin(Long targetUserId) {
        return isAdmin() || id.equals(targetUserId);
    }
}
