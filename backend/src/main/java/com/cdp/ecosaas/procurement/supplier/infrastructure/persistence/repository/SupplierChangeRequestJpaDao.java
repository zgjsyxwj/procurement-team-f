package com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.repository;

import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity.SupplierChangeRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 供应商信息变更申请/记录 Spring Data JPA 数据访问接口。
 */
public interface SupplierChangeRequestJpaDao extends JpaRepository<SupplierChangeRequestEntity, Long> {

    List<SupplierChangeRequestEntity> findBySupplierIdOrderBySubmittedAtDesc(Long supplierId);

    List<SupplierChangeRequestEntity> findBySupplierIdAndStatus(Long supplierId, String status);

    List<SupplierChangeRequestEntity> findByStatus(String status);

    /**
     * 提交早于阈值且仍处于指定状态的变更（24h 未审核再提醒，Req 5.8）。
     */
    List<SupplierChangeRequestEntity> findByStatusAndSubmittedAtBefore(String status, LocalDateTime threshold);
}
