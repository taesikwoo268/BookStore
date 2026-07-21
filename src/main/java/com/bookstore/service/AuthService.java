package com.bookstore.service;

import com.bookstore.dto.request.LoginRequest;
import com.bookstore.dto.request.RefreshTokenRequest;
import com.bookstore.dto.request.RegisterRequest;
import com.bookstore.dto.response.AuthResponse;
import com.bookstore.dto.response.TokenRefreshResponse;
import com.bookstore.exception.DuplicateEmailException;
import com.bookstore.exception.DuplicateUsernameException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.model.RefreshToken;
import com.bookstore.model.Role;
import com.bookstore.model.User;
import com.bookstore.repository.RoleRepository;
import com.bookstore.repository.UserRepository;
import com.bookstore.security.CustomUserDetails;
import com.bookstore.security.JwtRsaProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.control.MappingControl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtRsaProvider jwtRsaProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private static final String DEFAULT_ROLE = "ROLE_USER";

    // ============================================================
    // 1. REGISTER
    // ============================================================

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("📝 Registering new user: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUsernameException("Username already exists: " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already exists: " + request.getEmail());
        }

        Role defaultRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found: " + DEFAULT_ROLE));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .enabled(true)
                .roles(List.of(defaultRole))
                .build();

        User savedUser = userRepository.save(user);
        log.info("✅ User registered successfully: {}", savedUser.getUsername());

        // Tạo token cho user mới
        String accessToken = jwtRsaProvider.generateAccessToken(savedUser.getUsername());
        String refreshToken = refreshTokenService.createRefreshToken(savedUser,null,null);

        return buildAuthResponse(savedUser, accessToken, refreshToken);
    }

    // ============================================================
    // 2. LOGIN
    // ============================================================

    public AuthResponse login(LoginRequest request, HttpServletRequest httpServletRequest) {
        log.info("🪪 Login user: {}", request.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser()
                    ;
            // Cập nhật last login
            user.setLastLogin(Instant.now());
            userRepository.save(user);

            String ipAddress = getClientIp(httpServletRequest);
            String userAgent = httpServletRequest.getHeader("User-Agent");

            String accessToken = jwtRsaProvider.generateAccessToken(authentication);
            String refreshToken = refreshTokenService.createRefreshToken(user, ipAddress, userAgent);

            log.info("✅ User logged in successfully: {}", user.getUsername());
            return buildAuthResponse(user, accessToken, refreshToken);
        }
        catch (BadCredentialsException ex) {
            log.warn("❌ Login failed for user: {} - Invalid credentials", request.getUsername());
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    // ============================================================
    // 3. REFRESH TOKEN
    // ============================================================

    public TokenRefreshResponse refreshToken(RefreshTokenRequest request,HttpServletRequest httpServletRequest) {
        log.info("🔄️Refreshing token");

        String oldPlainToken = request.getRefreshToken();
        RefreshToken oldRefreshToken = refreshTokenService.validateAndGetRefreshToken(oldPlainToken);
        User user = oldRefreshToken.getUser();

        String ipAddress = getClientIp(httpServletRequest);
        String userAgent = httpServletRequest.getHeader("User-Agent");

        String newRefreshToken = refreshTokenService.rotateRefreshToken(oldPlainToken, user, ipAddress, userAgent);

         String newAccessToken = jwtRsaProvider.generateAccessToken(user.getUsername());
         log.info("✅ Token refreshed successfully for user: {}", user.getUsername());
         return TokenRefreshResponse.builder()
                 .accessToken(newAccessToken)
                 .refreshToken(newRefreshToken)
                 .tokenType("Bearer")
                 .expiresIn(jwtRsaProvider.getAccessTokenExpiration() / 1000)
                 .build();
    }

    // ============================================================
    // 4. LOGOUT
    // ============================================================

    @Transactional
    public void logout(String refreshToken) {
        log.info("🪦Logging out user");

        if (refreshToken != null && !refreshToken.isEmpty()) {
            refreshTokenService.blacklistRefreshToken(refreshToken);
            log.info("Refresh token blacklisted successfully");
        }

        SecurityContextHolder.clearContext();
        log.info("User logged out successfully");
    }


    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtRsaProvider.getAccessTokenExpiration() / 1000)
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()))
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }

        String xri = request.getHeader("X-Real-IP");
        if (xri != null && !xri.isBlank()) {
            return xri.trim();
        }

        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr : "unknown";
    }
}