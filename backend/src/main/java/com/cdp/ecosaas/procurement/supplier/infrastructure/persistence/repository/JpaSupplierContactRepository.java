package com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.repository;

import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierContact;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierContactRepository;
import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity.SupplierContactEntity;
import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.mapper.SupplierContactMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * SupplierContactRepository 的 JPA 实现。更新时重载托管实体以保留审计字段。
 */
@Repository
@RequiredArgsConstructor
public class JpaSupplierContactRepository implements SupplierContactRepository {

    private final SupplierContactJpaDao jpaDao;
    private final SupplierContactMapper mapper;

    @Override
    public SupplierContact save(SupplierContact contact) {
        SupplierContactEntity entity;
        if (contact.getId() != null) {
            entity = jpaDao.findById(contact.getId()).orElseGet(() -> mapper.toEntity(contact));
            mapper.updateEntity(contact, entity);
        } else {
            entity = mapper.toEntity(contact);
        }
        return mapper.toDomain(jpaDao.save(entity));
    }

    @Override
    @Transactional
    public List<SupplierContact> saveAll(List<SupplierContact> contacts) {
        return contacts.stream().map(this::save).toList();
    }

    @Override
    public Optional<SupplierContact> findById(Long id) {
        return jpaDao.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<SupplierContact> findBySupplierId(Long supplierId) {
        return jpaDao.findBySupplierId(supplierId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(Long id) {
        jpaDao.deleteById(id);
    }
}
