package com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.repository;

import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity.SupplierCertificateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 供应商证件 Spring Data JPA 数据访问接口。
 */
public interface SupplierCertificateJpaDao extends JpaRepository<SupplierCertificateEntity, Long> {

    List<SupplierCertificateEntity> findBySupplierId(Long supplierId);

    /**
     * 已通过审核且当前有效的证件（供到期提醒扫描，Req 12.1）。
     */
    List<SupplierCertificateEntity> findByAuditStatusAndCurrentValid(String auditStatus, boolean currentValid);
}
