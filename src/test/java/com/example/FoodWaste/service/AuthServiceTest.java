package com.example.FoodWaste.service;

import com.example.FoodWaste.dto.AuthRequest;
import com.example.FoodWaste.dto.AuthResponse;
import com.example.FoodWaste.dto.RegisterRequest;
import com.example.FoodWaste.dto.UserResponse;
import com.example.FoodWaste.entity.Role;
import com.example.FoodWaste.entity.User;
import com.example.FoodWaste.repository.UserRepository;
import com.example.FoodWaste.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {

        registerRequest = new RegisterRequest();
        registerRequest.setName("Jane Donor");
        registerRequest.setEmail("jane@example.com");
        registerRequest.setPassword("plaintext-password");
        registerRequest.setRole("DONOR");
    }

    @Test
    void register_hashesPasswordAndNeverStoresPlaintext() {

        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plaintext-password")).thenReturn("hashed-value");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        UserResponse response = authService.register(registerRequest);

        verify(passwordEncoder).encode("plaintext-password");
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getRole()).isEqualTo("DONOR");
    }

    @Test
    void register_rejectsDuplicateEmail() {

        when(userRepository.findByEmail(registerRequest.getEmail()))
                .thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_rejectsAdminRoleSelfEscalation() {

        registerRequest.setRole("ADMIN");

        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_rejectsUnknownRole() {

        registerRequest.setRole("SUPERUSER");

        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void login_succeedsWithCorrectPassword() {

        User user = User.builder()
                .id(1L)
                .email("jane@example.com")
                .password("hashed-value")
                .name("Jane Donor")
                .role(Role.DONOR)
                .build();

        AuthRequest request = new AuthRequest();
        request.setEmail("jane@example.com");
        request.setPassword("plaintext-password");

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plaintext-password", "hashed-value")).thenReturn(true);
        when(jwtUtil.generateToken("jane@example.com")).thenReturn("signed-jwt");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("signed-jwt");
        assertThat(response.getRole()).isEqualTo("DONOR");
    }

    @Test
    void login_rejectsWrongPasswordWithoutRevealingWhichFieldWasWrong() {

        User user = User.builder()
                .id(1L)
                .email("jane@example.com")
                .password("hashed-value")
                .role(Role.DONOR)
                .build();

        AuthRequest request = new AuthRequest();
        request.setEmail("jane@example.com");
        request.setPassword("wrong-password");

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_rejectsUnknownEmailWithSameErrorAsWrongPassword() {

        AuthRequest request = new AuthRequest();
        request.setEmail("nobody@example.com");
        request.setPassword("whatever");

        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }
}
