package com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.repository;

import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity.SupplierInvitationLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 供应商邀请日志 Spring Data JPA 数据访问接口。
 */
public interface SupplierInvitationLogJpaDao extends JpaRepository<SupplierInvitationLogEntity, Long> {
}
