package com.cdp.ecosaas.procurement.supplier.domain.repository;

import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierContact;

import java.util.List;
import java.util.Optional;

/**
 * 供应商联系人仓储接口（领域层端口），由基础设施层实现（任务 5.3）。
 */
public interface SupplierContactRepository {

    SupplierContact save(SupplierContact contact);

    /**
     * 批量保存（设主要联系人时联动更新多条记录）。
     */
    List<SupplierContact> saveAll(List<SupplierContact> contacts);

    Optional<SupplierContact> findById(Long id);

    List<SupplierContact> findBySupplierId(Long supplierId);

    void deleteById(Long id);
}
