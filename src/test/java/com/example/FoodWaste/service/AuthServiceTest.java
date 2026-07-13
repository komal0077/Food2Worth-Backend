package com.example.FoodWaste.service;

import com.example.FoodWaste.dto.AuthRequest;
import com.example.FoodWaste.dto.AuthResponse;
import com.example.FoodWaste.dto.RegisterRequest;
import com.example.FoodWaste.dto.UserResponse;
import com.example.FoodWaste.entity.User;
import com.example.FoodWaste.repository.UserRepository;
import com.example.FoodWaste.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    // Real BCrypt encoder - hashing behaviour is exactly what these tests need to verify
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, jwtUtil, passwordEncoder);
    }

    @Test
    void registerHashesPasswordAndNeverStoresPlaintext() {

        RegisterRequest request = new RegisterRequest();
        request.setName("Ann Donor");
        request.setEmail("ann@example.com");
        request.setPassword("plaintext123");
        request.setPhone("9999999999");
        request.setRole("DONOR");

        when(userRepository.findByEmail("ann@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User saved = inv.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        UserResponse response = authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User persisted = captor.getValue();
        assertThat(persisted.getPassword()).isNotEqualTo("plaintext123");
        assertThat(passwordEncoder.matches("plaintext123", persisted.getPassword())).isTrue();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("ann@example.com");
    }

    @Test
    void registerRejectsDuplicateEmail() {

        RegisterRequest request = new RegisterRequest();
        request.setEmail("ann@example.com");
        request.setPassword("plaintext123");
        request.setName("Ann");
        request.setRole("DONOR");

        when(userRepository.findByEmail("ann@example.com"))
                .thenReturn(Optional.of(User.builder().id(1L).email("ann@example.com").build()));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void loginSucceedsWithCorrectPassword() {

        String hashed = passwordEncoder.encode("correct-password");

        User user = User.builder()
                .id(1L)
                .email("ann@example.com")
                .password(hashed)
                .name("Ann Donor")
                .role("DONOR")
                .build();

        when(userRepository.findByEmail("ann@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("ann@example.com")).thenReturn("signed-jwt");

        AuthRequest request = new AuthRequest();
        request.setEmail("ann@example.com");
        request.setPassword("correct-password");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("signed-jwt");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getRole()).isEqualTo("DONOR");
    }

    @Test
    void loginRejectsWrongPassword() {

        User user = User.builder()
                .id(1L)
                .email("ann@example.com")
                .password(passwordEncoder.encode("correct-password"))
                .build();

        when(userRepository.findByEmail("ann@example.com")).thenReturn(Optional.of(user));

        AuthRequest request = new AuthRequest();
        request.setEmail("ann@example.com");
        request.setPassword("wrong-password");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void loginRejectsUnknownEmailWithSameGenericMessageAsWrongPassword() {

        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        AuthRequest request = new AuthRequest();
        request.setEmail("nobody@example.com");
        request.setPassword("whatever");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");
    }
}
