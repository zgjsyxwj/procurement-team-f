package com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.repository;

import com.cdp.ecosaas.procurement.supplier.domain.model.CertificateAuditStatus;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierCertificate;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierCertificateRepository;
import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity.SupplierCertificateEntity;
import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.mapper.SupplierCertificateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * SupplierCertificateRepository 的 JPA 实现。更新时重载托管实体以保留审计字段。
 */
@Repository
@RequiredArgsConstructor
public class JpaSupplierCertificateRepository implements SupplierCertificateRepository {

    private final SupplierCertificateJpaDao jpaDao;
    private final SupplierCertificateMapper mapper;

    @Override
    public SupplierCertificate save(SupplierCertificate certificate) {
        SupplierCertificateEntity entity;
        if (certificate.getId() != null) {
            entity = jpaDao.findById(certificate.getId()).orElseGet(() -> mapper.toEntity(certificate));
            mapper.updateEntity(certificate, entity);
        } else {
            entity = mapper.toEntity(certificate);
        }
        return mapper.toDomain(jpaDao.save(entity));
    }

    @Override
    public Optional<SupplierCertificate> findById(Long id) {
        return jpaDao.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<SupplierCertificate> findBySupplierId(Long supplierId) {
        return jpaDao.findBySupplierId(supplierId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<SupplierCertificate> findCurrentValidApproved() {
        return jpaDao.findByAuditStatusAndCurrentValid(CertificateAuditStatus.APPROVED.name(), true)
                .stream().map(mapper::toDomain).toList();
    }
}
