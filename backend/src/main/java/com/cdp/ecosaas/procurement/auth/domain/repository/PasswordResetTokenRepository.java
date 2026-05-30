package com.cdp.ecosaas.procurement.auth.domain.repository;

import com.cdp.ecosaas.procurement.auth.domain.model.PasswordResetToken;

import java.util.Optional;

/**
 * 密码重置令牌仓储接口（领域层端口）
 * <p>
 * 定义密码重置令牌持久化的抽象契约，由基础设施层实现。
 */
public interface PasswordResetTokenRepository {

    /**
     * 按 token 值查询密码重置令牌。
     *
     * @param token 令牌值
     * @return 令牌（如果存在）
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * 保存密码重置令牌。
     *
     * @param resetToken 令牌领域对象
     */
    void save(PasswordResetToken resetToken);

    /**
     * 标记令牌为已使用。
     *
     * @param token 令牌值
     */
    void markAsUsed(String token);
}
