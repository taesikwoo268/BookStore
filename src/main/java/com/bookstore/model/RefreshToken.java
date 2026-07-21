package com.bookstore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_id", nullable = false, unique = true, length = 36)
    private String tokenId;  // UUID

    @Column(nullable = false, unique = true, length = 255)
    private String tokenHash; // Bcrypt

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revoked")
    @Builder.Default
    private Boolean revoked = false;

    @Column(name = "revoked_reason", length = 100)
    private String revokedReason;  // "LOGOUT", "ROTATED", "EXPIRED"

    @Column(name = "parent_token_id")
    private Long parentTokenId;  // ✅ Theo dõi token rotation chain

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    // ✅ Helper methods
    public boolean isExpired() {
        return Instant.now().isAfter(expiryDate);
    }

    public boolean isActive() {
        return !revoked && !isExpired();
    }
}