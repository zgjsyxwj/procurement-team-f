package com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.repository;

import com.cdp.ecosaas.procurement.supplier.domain.model.ChangeRequestStatus;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierChangeField;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierChangeRequest;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierChangeRequestRepository;
import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity.SupplierChangeFieldEntity;
import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity.SupplierChangeRequestEntity;
import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.mapper.SupplierChangeRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SupplierChangeRequestRepository 的 JPA 实现。变更字段以 “按 change_request_id 整体替换” 维护。
 */
@Repository
@RequiredArgsConstructor
public class JpaSupplierChangeRequestRepository implements SupplierChangeRequestRepository {

    private final SupplierChangeRequestJpaDao requestDao;
    private final SupplierChangeFieldJpaDao fieldDao;
    private final SupplierChangeRequestMapper mapper;

    @Override
    @Transactional
    public SupplierChangeRequest save(SupplierChangeRequest changeRequest) {
        SupplierChangeRequestEntity entity;
        if (changeRequest.getId() != null) {
            entity = requestDao.findById(changeRequest.getId()).orElseGet(() -> mapper.toEntity(changeRequest));
            mapper.updateEntity(changeRequest, entity);
        } else {
            entity = mapper.toEntity(changeRequest);
        }
        SupplierChangeRequestEntity saved = requestDao.save(entity);
        syncFields(saved.getId(), changeRequest.getFields());
        return assemble(saved);
    }

    private void syncFields(Long changeRequestId, List<SupplierChangeField> fields) {
        fieldDao.deleteByChangeRequestId(changeRequestId);
        if (fields == null || fields.isEmpty()) {
            return;
        }
        List<SupplierChangeFieldEntity> entities = new ArrayList<>();
        for (SupplierChangeField field : fields) {
            SupplierChangeFieldEntity fieldEntity = mapper.toFieldEntity(field);
            fieldEntity.setChangeRequestId(changeRequestId);
            entities.add(fieldEntity);
        }
        fieldDao.saveAll(entities);
    }

    private SupplierChangeRequest assemble(SupplierChangeRequestEntity entity) {
        List<SupplierChangeField> fields = mapper.toFieldDomains(
                fieldDao.findByChangeRequestId(entity.getId()));
        return mapper.toDomain(entity, fields);
    }

    @Override
    public Optional<SupplierChangeRequest> findById(Long id) {
        return requestDao.findById(id).map(this::assemble);
    }

    @Override
    public List<SupplierChangeRequest> findBySupplierId(Long supplierId) {
        return requestDao.findBySupplierIdOrderBySubmittedAtDesc(supplierId).stream().map(this::assemble).toList();
    }

    @Override
    public List<SupplierChangeRequest> findPendingBySupplierId(Long supplierId) {
        return requestDao.findBySupplierIdAndStatus(supplierId, ChangeRequestStatus.PENDING_REVIEW.name())
                .stream().map(this::assemble).toList();
    }

    @Override
    public List<SupplierChangeRequest> findByStatus(ChangeRequestStatus status) {
        return requestDao.findByStatus(status.name()).stream().map(this::assemble).toList();
    }

    @Override
    public List<SupplierChangeRequest> findPendingSubmittedBefore(LocalDateTime threshold) {
        return requestDao.findByStatusAndSubmittedAtBefore(ChangeRequestStatus.PENDING_REVIEW.name(), threshold)
                .stream().map(this::assemble).toList();
    }
}
