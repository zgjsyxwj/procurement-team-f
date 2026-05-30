package com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.repository;

import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity.SupplierChangeFieldEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 供应商变更字段明细 Spring Data JPA 数据访问接口。
 */
public interface SupplierChangeFieldJpaDao extends JpaRepository<SupplierChangeFieldEntity, Long> {

    List<SupplierChangeFieldEntity> findByChangeRequestId(Long changeRequestId);

    void deleteByChangeRequestId(Long changeRequestId);
}
