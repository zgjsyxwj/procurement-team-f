package com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.repository;

import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity.CertTypeFieldEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 证件类型差异化字段 Spring Data JPA 数据访问接口。
 */
public interface CertTypeFieldJpaDao extends JpaRepository<CertTypeFieldEntity, Long> {

    List<CertTypeFieldEntity> findByCertTypeIdOrderBySortOrderAsc(Long certTypeId);

    void deleteByCertTypeId(Long certTypeId);
}
