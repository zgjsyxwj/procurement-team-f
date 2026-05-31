package com.cdp.ecosaas.procurement.auth.infrastructure.persistence.repository;

import com.cdp.ecosaas.procurement.auth.infrastructure.persistence.entity.SupplierUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 供应商用户 Spring Data JPA 数据访问接口。
 */
public interface SupplierUserJpaDao extends JpaRepository<SupplierUserEntity, Long> {

    Optional<SupplierUserEntity> findByPhone(String phone);

    Optional<SupplierUserEntity> findByEmail(String email);

    Optional<SupplierUserEntity> findBySupplierId(Long supplierId);
}
