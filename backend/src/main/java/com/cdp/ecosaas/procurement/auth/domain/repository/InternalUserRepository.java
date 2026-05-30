package com.cdp.ecosaas.procurement.auth.domain.repository;

import com.cdp.ecosaas.procurement.auth.domain.model.InternalUser;

import java.util.Optional;

/**
 * 内部用户仓储接口（领域层端口）
 * <p>
 * 定义内部用户持久化的抽象契约，由基础设施层实现。
 */
public interface InternalUserRepository {

    Optional<InternalUser> findById(Long id);

    Optional<InternalUser> findByPhone(String phone);

    Optional<InternalUser> findByEmail(String email);

    Optional<InternalUser> findBySsoSubjectId(String ssoSubjectId);

    InternalUser save(InternalUser user);
}
