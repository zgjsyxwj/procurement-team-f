package com.cdp.ecosaas.procurement.auth.domain.repository;

import com.cdp.ecosaas.procurement.auth.domain.model.SupplierUser;

import java.util.Optional;

/**
 * 供应商用户仓储接口（领域层端口）
 * <p>
 * 定义供应商用户持久化的抽象契约，由基础设施层实现。
 */
public interface SupplierUserRepository {

    Optional<SupplierUser> findById(Long id);

    Optional<SupplierUser> findByPhone(String phone);

    Optional<SupplierUser> findByEmail(String email);

    SupplierUser save(SupplierUser user);
}
