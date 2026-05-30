package com.cdp.ecosaas.procurement.auth.infrastructure.persistence.repository;

import com.cdp.ecosaas.procurement.auth.infrastructure.persistence.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 审计日志 Spring Data JPA 数据访问接口。
 * <p>
 * 继承 JpaSpecificationExecutor 以支持按事件类型、时间范围、目标账号的动态分页查询。
 */
public interface AuditLogJpaDao extends JpaRepository<AuditLogEntity, Long>,
        JpaSpecificationExecutor<AuditLogEntity> {
}
