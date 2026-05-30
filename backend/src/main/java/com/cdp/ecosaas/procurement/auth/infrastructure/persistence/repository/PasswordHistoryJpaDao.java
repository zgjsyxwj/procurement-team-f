package com.cdp.ecosaas.procurement.auth.infrastructure.persistence.repository;

import com.cdp.ecosaas.procurement.auth.infrastructure.persistence.entity.PasswordHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 密码历史 Spring Data JPA 数据访问接口。
 */
public interface PasswordHistoryJpaDao extends JpaRepository<PasswordHistoryEntity, Long> {

    /**
     * 查询指定用户最近5条密码历史记录，按创建时间倒序排列。
     */
    List<PasswordHistoryEntity> findTop5ByUserIdAndUserTypeOrderByCreatedAtDesc(Long userId, String userType);
}
