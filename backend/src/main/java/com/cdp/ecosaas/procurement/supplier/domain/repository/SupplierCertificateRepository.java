package com.cdp.ecosaas.procurement.supplier.domain.repository;

import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierCertificate;

import java.util.List;
import java.util.Optional;

/**
 * 供应商证件仓储接口（领域层端口），由基础设施层实现（任务 5.3）。
 */
public interface SupplierCertificateRepository {

    SupplierCertificate save(SupplierCertificate certificate);

    Optional<SupplierCertificate> findById(Long id);

    List<SupplierCertificate> findBySupplierId(Long supplierId);

    /**
     * 查询所有已通过审核且当前有效的证件（供证件到期提醒定时任务扫描，Req 12.1）。
     */
    List<SupplierCertificate> findCurrentValidApproved();
}
