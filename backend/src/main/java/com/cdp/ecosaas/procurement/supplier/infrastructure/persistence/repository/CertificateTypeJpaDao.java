package com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.repository;

import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity.CertificateTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 证件类型字典 Spring Data JPA 数据访问接口。
 */
public interface CertificateTypeJpaDao extends JpaRepository<CertificateTypeEntity, Long> {

    List<CertificateTypeEntity> findByStatus(String status);

    boolean existsByName(String name);
}
