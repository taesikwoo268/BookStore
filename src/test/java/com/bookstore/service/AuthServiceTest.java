package com.bookstore.service;

import com.bookstore.dto.request.RegisterRequest;
import com.bookstore.dto.response.AuthResponse;
import com.bookstore.exception.DuplicateEmailException;
import com.bookstore.exception.DuplicateUsernameException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.model.Role;
import com.bookstore.model.User;
import com.bookstore.repository.RoleRepository;
import com.bookstore.repository.UserRepository;
import com.bookstore.security.JwtRsaProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtRsaProvider jwtRsaProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private User user;
    private Role defaultRole;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("testuser")
                .email("test@email.com")
                .password("Test@123456")
                .fullName("Test User")
                .build();

        defaultRole = Role.builder()
                .id(1L)
                .name("ROLE_USER")
                .description("Default user role")
                .build();

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@email.com")
                .password("encoded_password")
                .fullName("Test User")
                .enabled(true)
                .roles(List.of(defaultRole))
                .build();
    }

    // ============================================================
    // 1. REGISTER - SUCCESS
    // ============================================================

    @Test
    @DisplayName("register - new email and username success")
    void register_newEmailAndUsername_success() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtRsaProvider.generateAccessToken(anyString())).thenReturn("access_token");
        when(refreshTokenService.createRefreshToken(any(), any(), any())).thenReturn("refresh_token_123");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("test@email.com", response.getEmail());
        assertEquals("Test User", response.getFullName());
        assertEquals("access_token", response.getAccessToken());
        assertEquals("refresh_token_123", response.getRefreshToken());
        assertTrue(response.getRoles().contains("ROLE_USER"));

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@email.com");
        verify(roleRepository).findByName("ROLE_USER");
        verify(passwordEncoder).encode("Test@123456");
        verify(userRepository).save(any(User.class));
        verify(jwtRsaProvider).generateAccessToken("testuser");
        verify(refreshTokenService).createRefreshToken(any(), any(), any());
    }

    @Test
    @DisplayName("register - new email success, username valid")
    void register_newEmail_success() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtRsaProvider.generateAccessToken(anyString())).thenReturn("access_token");
        when(refreshTokenService.createRefreshToken(any(), any(), any())).thenReturn("refresh_token_123");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("test@email.com", response.getEmail());
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register - encode password correctly")
    void register_encodePasswordCorrectly() {
        String rawPassword = "Test@123456";
        String encodedPassword = "$2a$10$encoded_password_hash";

        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtRsaProvider.generateAccessToken(anyString())).thenReturn("access_token");
        when(refreshTokenService.createRefreshToken(any(), any(), any())).thenReturn("refresh_token_123");

        authService.register(registerRequest);

        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(argThat(savedUser ->
                savedUser.getPassword().equals(encodedPassword)
        ));
    }

    @Test
    @DisplayName("register - sets default role ROLE_USER")
    void register_setsDefaultRole() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtRsaProvider.generateAccessToken(anyString())).thenReturn("access_token");
        when(refreshTokenService.createRefreshToken(any(), any(), any())).thenReturn("refresh_token_123");

        authService.register(registerRequest);

        verify(roleRepository).findByName("ROLE_USER");
        verify(userRepository).save(argThat(savedUser ->
                savedUser.getRoles() != null &&
                        !savedUser.getRoles().isEmpty() &&
                        savedUser.getRoles().get(0).getName().equals("ROLE_USER")
        ));
    }

    @Test
    @DisplayName("register - generates access token after saving user")
    void register_generatesAccessToken() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtRsaProvider.generateAccessToken(anyString())).thenReturn("access_token");
        when(refreshTokenService.createRefreshToken(any(), any(), any())).thenReturn("refresh_token_123");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response.getAccessToken());
        verify(jwtRsaProvider).generateAccessToken(user.getUsername());
    }

    @Test
    @DisplayName("register - generates refresh token after saving user")
    void register_generatesRefreshToken() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtRsaProvider.generateAccessToken(anyString())).thenReturn("access_token");
        when(refreshTokenService.createRefreshToken(any(), any(), any())).thenReturn("refresh_token_123");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response.getRefreshToken());
        verify(refreshTokenService).createRefreshToken(any(), any(), any());
    }

    // ============================================================
    // 2. REGISTER - EXCEPTIONS
    // ============================================================

    @Test
    @DisplayName("register - duplicate username throws DuplicateUsernameException")
    void register_duplicateUsername_throwsException() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateUsernameException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, never()).existsByEmail(anyString());
        verify(roleRepository, never()).findByName(any());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(refreshTokenService, never()).createRefreshToken(any(), any(), any());
    }

    @Test
    @DisplayName("register - duplicate email throws DuplicateEmailException")
    void register_duplicateEmail_throwsException() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("Email already exists");

        verify(roleRepository, never()).findByName(any());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(refreshTokenService, never()).createRefreshToken(any(), any(), any());
    }

    @Test
    @DisplayName("register - default role not found throws ResourceNotFoundException")
    void register_defaultRoleNotFound_throwsException() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Default role not found");

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(refreshTokenService, never()).createRefreshToken(any(), any(), any());
    }

    @Test
    @DisplayName("register - repository save throws exception")
    void register_repositorySaveThrowsException() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database error");

        verify(jwtRsaProvider, never()).generateAccessToken(anyString());
        verify(refreshTokenService, never()).createRefreshToken(any(), any(), any());
    }

    // ============================================================
    // 3. REGISTER - EDGE CASES
    // ============================================================

    @Test
    @DisplayName("register - username with special characters")
    void register_usernameWithSpecialCharacters_success() {
        RegisterRequest request = RegisterRequest.builder()
                .username("test_user_123")
                .email("test.special@email.com")
                .password("Test@123456")
                .fullName("Test User")
                .build();

        User userWithSpecial = User.builder()
                .id(1L)
                .username("test_user_123")
                .email("test.special@email.com")
                .password("encoded_password")
                .fullName("Test User")
                .enabled(true)
                .roles(List.of(defaultRole))
                .build();

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(userWithSpecial);
        when(jwtRsaProvider.generateAccessToken(anyString())).thenReturn("access_token");
        when(refreshTokenService.createRefreshToken(any(), any(), any())).thenReturn("refresh_token_123");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("test_user_123", response.getUsername());
        assertEquals("test.special@email.com", response.getEmail());
    }

    @Test
    @DisplayName("register - empty full name")
    void register_emptyFullName_success() {
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser2")
                .email("test2@email.com")
                .password("Test@123456")
                .fullName("")
                .build();

        User userWithEmptyName = User.builder()
                .id(1L)
                .username("testuser2")
                .email("test2@email.com")
                .password("encoded_password")
                .fullName("")
                .enabled(true)
                .roles(List.of(defaultRole))
                .build();

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(userWithEmptyName);
        when(jwtRsaProvider.generateAccessToken(anyString())).thenReturn("access_token");
        when(refreshTokenService.createRefreshToken(any(), any(), any())).thenReturn("refresh_token_123");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("", response.getFullName());
    }

    @Test
    @DisplayName("register - verify user is enabled by default")
    void register_userEnabledByDefault() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtRsaProvider.generateAccessToken(anyString())).thenReturn("access_token");
        when(refreshTokenService.createRefreshToken(any(), any(), any())).thenReturn("refresh_token_123");

        authService.register(registerRequest);

        verify(userRepository).save(argThat(savedUser ->
                savedUser.getEnabled() != null && savedUser.getEnabled()
        ));
    }
}