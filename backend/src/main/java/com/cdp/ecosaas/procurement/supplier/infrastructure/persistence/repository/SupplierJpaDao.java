package com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.repository;

import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity.SupplierEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * 供应商 Spring Data JPA 数据访问接口。
 * <p>
 * 继承 JpaSpecificationExecutor 以支持按名称模糊 + 状态的动态分页查询（Req 8）。
 */
public interface SupplierJpaDao extends JpaRepository<SupplierEntity, Long>,
        JpaSpecificationExecutor<SupplierEntity> {

    Optional<SupplierEntity> findBySupplierCode(String supplierCode);

    List<SupplierEntity> findByStatus(String status);
}
