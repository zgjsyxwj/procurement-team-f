package com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.repository;

import com.cdp.ecosaas.procurement.supplier.domain.model.CertTypeField;
import com.cdp.ecosaas.procurement.supplier.domain.model.CertificateType;
import com.cdp.ecosaas.procurement.supplier.domain.model.CertificateTypeStatus;
import com.cdp.ecosaas.procurement.supplier.domain.repository.CertificateTypeRepository;
import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity.CertTypeFieldEntity;
import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity.CertificateTypeEntity;
import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.mapper.CertificateTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CertificateTypeRepository 的 JPA 实现。差异化字段以 “按 cert_type_id 整体替换” 维护。
 */
@Repository
@RequiredArgsConstructor
public class JpaCertificateTypeRepository implements CertificateTypeRepository {

    private final CertificateTypeJpaDao typeDao;
    private final CertTypeFieldJpaDao fieldDao;
    private final CertificateTypeMapper mapper;

    @Override
    @Transactional
    public CertificateType save(CertificateType certificateType) {
        CertificateTypeEntity entity;
        if (certificateType.getId() != null) {
            entity = typeDao.findById(certificateType.getId()).orElseGet(() -> mapper.toEntity(certificateType));
            mapper.updateEntity(certificateType, entity);
        } else {
            entity = mapper.toEntity(certificateType);
        }
        CertificateTypeEntity saved = typeDao.save(entity);
        syncFields(saved.getId(), certificateType.getFields());
        return assemble(saved);
    }

    private void syncFields(Long certTypeId, List<CertTypeField> fields) {
        fieldDao.deleteByCertTypeId(certTypeId);
        if (fields == null || fields.isEmpty()) {
            return;
        }
        List<CertTypeFieldEntity> entities = new ArrayList<>();
        for (CertTypeField field : fields) {
            CertTypeFieldEntity fieldEntity = mapper.toFieldEntity(field);
            fieldEntity.setCertTypeId(certTypeId);
            entities.add(fieldEntity);
        }
        fieldDao.saveAll(entities);
    }

    private CertificateType assemble(CertificateTypeEntity entity) {
        List<CertTypeField> fields = mapper.toFieldDomains(
                fieldDao.findByCertTypeIdOrderBySortOrderAsc(entity.getId()));
        return mapper.toDomain(entity, fields);
    }

    @Override
    public Optional<CertificateType> findById(Long id) {
        return typeDao.findById(id).map(this::assemble);
    }

    @Override
    public List<CertificateType> findAll() {
        return typeDao.findAll().stream().map(this::assemble).toList();
    }

    @Override
    public List<CertificateType> findByStatus(CertificateTypeStatus status) {
        return typeDao.findByStatus(status.name()).stream().map(this::assemble).toList();
    }

    @Override
    public boolean existsByName(String name) {
        return typeDao.existsByName(name);
    }
}
