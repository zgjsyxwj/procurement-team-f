package com.cdp.ecosaas.procurement.auth.infrastructure.persistence.repository;

import com.cdp.ecosaas.procurement.auth.infrastructure.persistence.entity.BuyerSupplierRelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 采购员-供应商关系 Spring Data JPA 数据访问接口。
 */
public interface BuyerSupplierRelationJpaDao extends JpaRepository<BuyerSupplierRelationEntity, Long> {

    Optional<BuyerSupplierRelationEntity> findByBuyerIdAndSupplierId(Long buyerId, Long supplierId);

    List<BuyerSupplierRelationEntity> findByBuyerId(Long buyerId);

    List<BuyerSupplierRelationEntity> findBySupplierId(Long supplierId);
}
