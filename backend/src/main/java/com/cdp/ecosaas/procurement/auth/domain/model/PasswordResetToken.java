package com.cdp.ecosaas.procurement.auth.domain.model;

import java.time.LocalDateTime;

/**
 * 密码重置令牌值对象
 * <p>
 * 表示一个密码重置令牌的领域概念，包含有效期和使用状态。
 */
public record PasswordResetToken(
        Long userId,
        String userType,
        String token,
        LocalDateTime expiresAt,
        boolean used,
        LocalDateTime createdAt
) {

    /**
     * 判断令牌是否已过期。
     */
    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * 判断令牌是否可用（未使用且未过期）。
     */
    public boolean isValid() {
        return !used && !isExpired();
    }
}
