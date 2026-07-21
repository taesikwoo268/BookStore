package com.bookstore.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
@Slf4j
public class JwtRsaProvider {

    @Value("${app.jwt.private-key-path}")
    private Resource privateKeyResource;

    @Value("${app.jwt.public-key-path}")
    private Resource publicKeyResource;

    @Value("${app.jwt.access-token-expiration}")
    @Getter
    private long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-expiration}")
    @Getter
    private long refreshTokenExpiration;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    // ===== LOAD KEYS =====
    private PrivateKey getPrivateKey() {
        if (privateKey == null) {
            try {
                privateKey = loadPrivateKey();
                log.info("✅ Private key loaded successfully");
            } catch (Exception e) {
                log.error("❌ Failed to load private key: {}", e.getMessage());
                throw new RuntimeException("Failed to load private key", e);
            }
        }
        return privateKey;
    }

    private PublicKey getPublicKey() {
        if (publicKey == null) {
            try {
                publicKey = loadPublicKey();
                log.info("✅ Public key loaded successfully");
            } catch (Exception e) {
                log.error("❌ Failed to load public key: {}", e.getMessage());
                throw new RuntimeException("Failed to load public key", e);
            }
        }
        return publicKey;
    }

    private PrivateKey loadPrivateKey() throws Exception {
        try (InputStream inputStream = privateKeyResource.getInputStream()) {
            String content = new String(inputStream.readAllBytes())
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(content);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        }
    }

    private PublicKey loadPublicKey() throws Exception {
        try (InputStream inputStream = publicKeyResource.getInputStream()) {
            String content = new String(inputStream.readAllBytes())
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(content);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        }
    }

    // ===== GENERATE TOKEN =====
    public String generateAccessToken(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return generateToken(userDetails.getUsername(), accessTokenExpiration);
    }

    public String generateAccessToken(String username) {
        return generateToken(username, accessTokenExpiration);
    }

    public String generateRefreshToken(String username) {
        return generateToken(username, refreshTokenExpiration);
    }

    private String generateToken(String username, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getPrivateKey(), Jwts.SIG.RS256)  // ✅ RS256
                .compact();
    }

    // ===== VALIDATE TOKEN =====
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getPublicKey())  // ✅ Dùng Public Key để verify
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    // ===== GET USERNAME FROM TOKEN =====
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }
}