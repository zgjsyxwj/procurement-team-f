package com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.repository;

import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity.SupplierBankAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 供应商银行账号 Spring Data JPA 数据访问接口。
 */
public interface SupplierBankAccountJpaDao extends JpaRepository<SupplierBankAccountEntity, Long> {

    List<SupplierBankAccountEntity> findBySupplierIdOrderBySortOrderAsc(Long supplierId);

    void deleteBySupplierId(Long supplierId);
}
