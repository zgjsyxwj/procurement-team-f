package com.cdp.ecosaas.procurement.auth.infrastructure.persistence.repository;

import com.cdp.ecosaas.procurement.auth.domain.model.PasswordHistory;
import com.cdp.ecosaas.procurement.auth.domain.repository.PasswordHistoryRepository;
import com.cdp.ecosaas.procurement.auth.infrastructure.persistence.entity.PasswordHistoryEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 密码历史仓储实现。
 * <p>
 * 实现领域层 PasswordHistoryRepository 接口，
 * 提供查询最近5条密码历史记录和保存新记录的功能。
 */
@Repository
@RequiredArgsConstructor
public class JpaPasswordHistoryRepository implements PasswordHistoryRepository {

    private final PasswordHistoryJpaDao jpaDao;

    @Override
    public List<PasswordHistory> findRecentByUser(Long userId, String userType) {
        List<PasswordHistoryEntity> entities = jpaDao.findTop5ByUserIdAndUserTypeOrderByCreatedAtDesc(userId, userType);
        return entities.stream()
                .map(e -> PasswordHistory.builder()
                        .userId(e.getUserId())
                        .userType(e.getUserType())
                        .passwordHash(e.getPasswordHash())
                        .createdAt(e.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    public void save(PasswordHistory passwordHistory) {
        PasswordHistoryEntity entity = PasswordHistoryEntity.builder()
                .userId(passwordHistory.getUserId())
                .userType(passwordHistory.getUserType())
                .passwordHash(passwordHistory.getPasswordHash())
                .createdAt(passwordHistory.getCreatedAt() != null ? passwordHistory.getCreatedAt() : LocalDateTime.now())
                .build();
        jpaDao.save(entity);
    }
}
