package com.cdp.ecosaas.procurement.auth.infrastructure.persistence.repository;

import com.cdp.ecosaas.procurement.auth.infrastructure.persistence.entity.InternalUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * 内部用户 Spring Data JPA 数据访问接口。
 * <p>
 * 继承 JpaSpecificationExecutor 以支持按角色、状态、关键字的动态分页查询。
 */
public interface InternalUserJpaDao extends JpaRepository<InternalUserEntity, Long>,
        JpaSpecificationExecutor<InternalUserEntity> {

    Optional<InternalUserEntity> findByPhone(String phone);

    Optional<InternalUserEntity> findByEmail(String email);

    Optional<InternalUserEntity> findBySsoSubjectId(String ssoSubjectId);
}
