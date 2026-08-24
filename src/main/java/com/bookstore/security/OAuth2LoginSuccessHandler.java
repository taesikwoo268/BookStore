package com.bookstore.security;

import com.bookstore.security.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.oauth2.success-url:http://localhost:3000/oauth2/redirect}")
    private String successUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        log.info("✅ OAuth2 Login success for user: {}", authentication.getName());

        // 1. Lấy user từ authentication
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        // 2. Tạo JWT token
        String accessToken = jwtTokenProvider.generateAccessToken(oAuth2User.getName());
        String refreshToken = jwtTokenProvider.generateRefreshToken(oAuth2User.getName());

        log.info("🔑 JWT tokens generated for OAuth2 user: {}", oAuth2User.getName());

        // 3. Redirect với token (Frontend sẽ lấy token từ URL)
        String redirectUrl = successUrl +
                "?accessToken=" + accessToken +
                "&refreshToken=" + refreshToken +
                "&userId=" + oAuth2User.getId() +
                "&username=" + oAuth2User.getName() +
                "&email=" + oAuth2User.getEmail() +
                "&fullName=" + oAuth2User.getFullName();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}