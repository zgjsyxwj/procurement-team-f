package com.cdp.ecosaas.procurement.auth.infrastructure.persistence.repository;

import com.cdp.ecosaas.procurement.auth.domain.model.InternalUser;
import com.cdp.ecosaas.procurement.auth.domain.repository.InternalUserRepository;
import com.cdp.ecosaas.procurement.auth.infrastructure.persistence.entity.InternalUserEntity;
import com.cdp.ecosaas.procurement.auth.infrastructure.persistence.mapper.InternalUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * InternalUserRepository 领域接口的 JPA 实现。
 * <p>
 * 委托 Spring Data JPA 的 InternalUserJpaDao 执行持久化操作，
 * 通过 InternalUserMapper 在领域对象与 JPA 实体之间进行转换。
 */
@Repository
@RequiredArgsConstructor
public class JpaInternalUserRepository implements InternalUserRepository {

    private final InternalUserJpaDao jpaDao;
    private final InternalUserMapper mapper;

    @Override
    public Optional<InternalUser> findById(Long id) {
        return jpaDao.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<InternalUser> findByPhone(String phone) {
        return jpaDao.findByPhone(phone).map(mapper::toDomain);
    }

    @Override
    public Optional<InternalUser> findByEmail(String email) {
        return jpaDao.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Optional<InternalUser> findBySsoSubjectId(String ssoSubjectId) {
        return jpaDao.findBySsoSubjectId(ssoSubjectId).map(mapper::toDomain);
    }

    @Override
    public InternalUser save(InternalUser user) {
        InternalUserEntity entity;
        if (user.getId() != null) {
            // Update existing: load managed entity to preserve version for optimistic locking
            entity = jpaDao.findById(user.getId())
                    .orElseGet(() -> mapper.toEntity(user));
            mapper.updateEntity(user, entity);
        } else {
            entity = mapper.toEntity(user);
        }
        InternalUserEntity saved = jpaDao.save(entity);
        return mapper.toDomain(saved);
    }
}
