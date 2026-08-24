package com.bookstore.security;

import com.bookstore.model.Role;
import com.bookstore.model.User;
import com.bookstore.repository.RoleRepository;
import com.bookstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private static final String DEFAULT_ROLE = "ROLE_USER";

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            // 1. Lấy user info từ Google
            OAuth2User oAuth2User = super.loadUser(userRequest);
            log.info("✅ OAuth2 User loaded: {}", oAuth2User.getAttributes());

            // 2. Lấy provider
            String provider = userRequest.getClientRegistration().getRegistrationId();

            // 3. Parse attributes
            Map<String, Object> attributes = oAuth2User.getAttributes();
            String providerId = (String) attributes.get("sub");
            String email = (String) attributes.get("email");
            String name = (String) attributes.get("name");
            String avatarUrl = (String) attributes.get("picture");
            Boolean emailVerified = (Boolean) attributes.get("email_verified");

            // 4. Tìm hoặc tạo user
            User user = findOrCreateUser(provider, providerId, email, name, avatarUrl, emailVerified);

            // 5. Trả về CustomOAuth2User
            return new CustomOAuth2User(user, attributes);

        } catch (Exception e) {
            log.error("❌ Failed to load OAuth2 user: {}", e.getMessage());
            throw new InternalAuthenticationServiceException(e.getMessage(), e);
        }
    }

    @Transactional
    protected User findOrCreateUser(String provider, String providerId, String email,
                                    String name, String avatarUrl, Boolean emailVerified) {
        log.info("🔍 Finding or creating user: provider={}, providerId={}, email={}",
                provider, providerId, email);

        // 1. Tìm user theo providerId
        Optional<User> existingUser = userRepository.findByProviderAndProviderId(provider, providerId);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            log.info("✅ Existing user found: {}", user.getUsername());

            // Cập nhật thông tin
            if (avatarUrl != null && !avatarUrl.equals(user.getAvatarUrl())) {
                user.setAvatarUrl(avatarUrl);
            }
            if (name != null && !name.equals(user.getFullName())) {
                user.setFullName(name);
            }
            if (emailVerified != null) {
                user.setEmailVerified(emailVerified);
            }
            user.setLastLogin(java.time.Instant.now());

            return userRepository.save(user);
        }

        // 2. Tìm user theo email (trường hợp đã đăng ký bằng email trước đó)
        Optional<User> userByEmail = userRepository.findByEmail(email);
        if (userByEmail.isPresent()) {
            User user = userByEmail.get();
            log.info("✅ User found by email, updating provider info: {}", user.getUsername());

            user.setProvider(provider);
            user.setProviderId(providerId);
            user.setAvatarUrl(avatarUrl);
            user.setEmailVerified(emailVerified != null && emailVerified);
            user.setEnabled(true);
            user.setLastLogin(java.time.Instant.now());

            return userRepository.save(user);
        }

        // 3. Tạo user mới
        log.info("🆕 Creating new OAuth2 user: {}", email);

        // Tạo username từ email
        String username = generateUsername(email);

        // Lấy role mặc định
        Role defaultRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new RuntimeException("Default role not found: " + DEFAULT_ROLE));

        // Tạo password ngẫu nhiên (không dùng)
        String randomPassword = java.util.UUID.randomUUID().toString();

        User newUser = User.builder()
                .username(username)
                .email(email)
                .password(randomPassword) // Sẽ không được dùng vì login qua OAuth2
                .fullName(name)
                .provider(provider)
                .providerId(providerId)
                .avatarUrl(avatarUrl)
                .emailVerified(emailVerified != null && emailVerified)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .lastLogin(java.time.Instant.now())
                .roles(List.of(defaultRole))
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("✅ New OAuth2 user created: {}", savedUser.getUsername());

        return savedUser;
    }

    private String generateUsername(String email) {
        // Lấy phần trước @
        String baseUsername = email.split("@")[0];

        // Kiểm tra username đã tồn tại chưa
        if (!userRepository.existsByUsername(baseUsername)) {
            return baseUsername;
        }

        // Nếu đã tồn tại, thêm số
        int counter = 1;
        String username;
        do {
            username = baseUsername + counter;
            counter++;
        } while (userRepository.existsByUsername(username));

        return username;
    }
}