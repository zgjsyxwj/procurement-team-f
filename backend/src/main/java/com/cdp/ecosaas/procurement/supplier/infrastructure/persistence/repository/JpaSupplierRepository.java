package com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.repository;

import com.cdp.ecosaas.procurement.shared.model.PageQuery;
import com.cdp.ecosaas.procurement.shared.model.PageResult;
import com.cdp.ecosaas.procurement.supplier.domain.model.Supplier;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierBankAccount;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierStatus;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierRepository;
import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity.SupplierBankAccountEntity;
import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity.SupplierEntity;
import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.mapper.SupplierMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SupplierRepository 的 JPA 实现。
 * <p>
 * 委托 {@link SupplierJpaDao} 持久化主表，并通过 {@link SupplierBankAccountJpaDao} 以
 * “按 supplier_id 整体替换” 的方式维护银行账号子记录（银行账号为值对象，无独立标识）。
 * 更新时重载托管实体以保留乐观锁版本（与 auth {@code JpaInternalUserRepository} 一致）。
 */
@Repository
@RequiredArgsConstructor
public class JpaSupplierRepository implements SupplierRepository {

    private final SupplierJpaDao supplierDao;
    private final SupplierBankAccountJpaDao bankAccountDao;
    private final SupplierMapper mapper;

    @Override
    @Transactional
    public Supplier save(Supplier supplier) {
        SupplierEntity entity;
        if (supplier.getId() != null) {
            entity = supplierDao.findById(supplier.getId()).orElseGet(() -> mapper.toEntity(supplier));
            mapper.updateEntity(supplier, entity);
        } else {
            entity = mapper.toEntity(supplier);
        }
        SupplierEntity saved = supplierDao.save(entity);
        syncBankAccounts(saved.getId(), supplier.getBankAccounts());
        return assemble(saved);
    }

    private void syncBankAccounts(Long supplierId, List<SupplierBankAccount> accounts) {
        bankAccountDao.deleteBySupplierId(supplierId);
        if (accounts == null || accounts.isEmpty()) {
            return;
        }
        List<SupplierBankAccountEntity> entities = new ArrayList<>();
        int order = 0;
        for (SupplierBankAccount account : accounts) {
            SupplierBankAccountEntity bankEntity = mapper.toBankEntity(account);
            bankEntity.setSupplierId(supplierId);
            bankEntity.setSortOrder(order++);
            entities.add(bankEntity);
        }
        bankAccountDao.saveAll(entities);
    }

    @Override
    public Optional<Supplier> findById(Long id) {
        return supplierDao.findById(id).map(this::assemble);
    }

    @Override
    public Optional<Supplier> findByCode(String supplierCode) {
        return supplierDao.findBySupplierCode(supplierCode).map(this::assemble);
    }

    private Supplier assemble(SupplierEntity entity) {
        List<SupplierBankAccount> banks = mapper.toBankDomains(
                bankAccountDao.findBySupplierIdOrderBySortOrderAsc(entity.getId()));
        return mapper.toDomain(entity, banks);
    }

    @Override
    public PageResult<Supplier> search(String nameKeyword, SupplierStatus status,
                                       java.util.Collection<Long> accessibleSupplierIds, PageQuery pageQuery) {
        Specification<SupplierEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (nameKeyword != null && !nameKeyword.isBlank()) {
                predicates.add(cb.like(root.<String>get("name"), "%" + nameKeyword + "%"));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status.name()));
            }
            // accessibleSupplierIds == null 表示不受限（ADMIN）；非 null 时按数据范围限定
            if (accessibleSupplierIds != null) {
                predicates.add(root.get("id").in(accessibleSupplierIds));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable = PageRequest.of(pageQuery.getPage(), pageQuery.getSize(), Sort.by("id").descending());
        // 列表不加载银行明细（避免 N+1）；详情页经 findById 加载
        Page<Supplier> page = supplierDao.findAll(spec, pageable)
                .map(entity -> mapper.toDomain(entity, List.of()));
        return PageResult.of(page);
    }

    @Override
    public List<Supplier> findByStatus(SupplierStatus status) {
        return supplierDao.findByStatus(status.name()).stream()
                .map(entity -> mapper.toDomain(entity, List.of()))
                .toList();
    }

    @Override
    public long nextCodeSequence() {
        return supplierDao.count() + 1;
    }
}
