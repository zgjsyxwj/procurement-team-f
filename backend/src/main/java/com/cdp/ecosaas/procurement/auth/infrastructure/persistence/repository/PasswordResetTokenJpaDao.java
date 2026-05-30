package com.cdp.ecosaas.procurement.auth.infrastructure.persistence.repository;

import com.cdp.ecosaas.procurement.auth.infrastructure.persistence.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 密码重置令牌 Spring Data JPA 数据访问接口。
 */
public interface PasswordResetTokenJpaDao extends JpaRepository<PasswordResetTokenEntity, Long> {

    /**
     * 按 token 值查询密码重置令牌。
     */
    Optional<PasswordResetTokenEntity> findByToken(String token);
}
