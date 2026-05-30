package com.cdp.ecosaas.procurement.auth.infrastructure.persistence.repository;

import com.cdp.ecosaas.procurement.auth.domain.model.PasswordResetToken;
import com.cdp.ecosaas.procurement.auth.domain.repository.PasswordResetTokenRepository;
import com.cdp.ecosaas.procurement.auth.infrastructure.persistence.entity.PasswordResetTokenEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 密码重置令牌仓储实现。
 * <p>
 * 实现领域层 PasswordResetTokenRepository 接口，
 * 提供按 token 查询、保存和标记已使用的功能。
 */
@Repository
@RequiredArgsConstructor
public class JpaPasswordResetTokenRepository implements PasswordResetTokenRepository {

    private final PasswordResetTokenJpaDao jpaDao;

    @Override
    public Optional<PasswordResetToken> findByToken(String token) {
        return jpaDao.findByToken(token).map(this::toDomain);
    }

    @Override
    public void save(PasswordResetToken resetToken) {
        PasswordResetTokenEntity entity = PasswordResetTokenEntity.builder()
                .userId(resetToken.userId())
                .userType(resetToken.userType())
                .token(resetToken.token())
                .expiresAt(resetToken.expiresAt())
                .used(resetToken.used())
                .createdAt(resetToken.createdAt())
                .build();
        jpaDao.save(entity);
    }

    @Override
    public void markAsUsed(String token) {
        jpaDao.findByToken(token).ifPresent(entity -> {
            entity.setUsed(true);
            jpaDao.save(entity);
        });
    }

    private PasswordResetToken toDomain(PasswordResetTokenEntity entity) {
        return new PasswordResetToken(
                entity.getUserId(),
                entity.getUserType(),
                entity.getToken(),
                entity.getExpiresAt(),
                entity.isUsed(),
                entity.getCreatedAt()
        );
    }
}
