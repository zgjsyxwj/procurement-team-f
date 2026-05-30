package com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.repository;

import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity.SupplierContactEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 供应商联系人 Spring Data JPA 数据访问接口。
 */
public interface SupplierContactJpaDao extends JpaRepository<SupplierContactEntity, Long> {

    List<SupplierContactEntity> findBySupplierId(Long supplierId);
}
