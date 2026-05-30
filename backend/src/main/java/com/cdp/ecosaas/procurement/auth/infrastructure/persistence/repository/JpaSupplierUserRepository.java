package com.cdp.ecosaas.procurement.auth.infrastructure.persistence.repository;

import com.cdp.ecosaas.procurement.auth.domain.model.SupplierUser;
import com.cdp.ecosaas.procurement.auth.domain.repository.SupplierUserRepository;
import com.cdp.ecosaas.procurement.auth.infrastructure.persistence.entity.SupplierUserEntity;
import com.cdp.ecosaas.procurement.auth.infrastructure.persistence.mapper.SupplierUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * SupplierUserRepository 领域接口的 JPA 实现。
 * <p>
 * 委托 Spring Data JPA 的 SupplierUserJpaDao 执行持久化操作，
 * 通过 SupplierUserMapper 在领域对象与 JPA 实体之间进行转换。
 */
@Repository
@RequiredArgsConstructor
public class JpaSupplierUserRepository implements SupplierUserRepository {

    private final SupplierUserJpaDao jpaDao;
    private final SupplierUserMapper mapper;

    @Override
    public Optional<SupplierUser> findById(Long id) {
        return jpaDao.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<SupplierUser> findByPhone(String phone) {
        return jpaDao.findByPhone(phone).map(mapper::toDomain);
    }

    @Override
    public Optional<SupplierUser> findByEmail(String email) {
        return jpaDao.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public SupplierUser save(SupplierUser user) {
        SupplierUserEntity entity;
        if (user.getId() != null) {
            entity = jpaDao.findById(user.getId())
                    .orElseGet(() -> mapper.toEntity(user));
            mapper.updateEntity(user, entity);
        } else {
            entity = mapper.toEntity(user);
        }
        SupplierUserEntity saved = jpaDao.save(entity);
        return mapper.toDomain(saved);
    }
}
