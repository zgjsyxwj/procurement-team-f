package com.cdp.ecosaas.procurement.supplier.domain.repository;

import com.cdp.ecosaas.procurement.supplier.domain.model.ChangeRequestStatus;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierChangeRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 供应商信息变更申请/记录仓储接口（领域层端口），由基础设施层实现（任务 5.3）。
 */
public interface SupplierChangeRequestRepository {

    SupplierChangeRequest save(SupplierChangeRequest changeRequest);

    Optional<SupplierChangeRequest> findById(Long id);

    /**
     * 某供应商的全部变更记录（按时间倒序，供变更历史展示，Req 50.2）。
     */
    List<SupplierChangeRequest> findBySupplierId(Long supplierId);

    /**
     * 某供应商的待审核变更（同类冲突校验 Req 3.6、当前待审核变更查询 Req 3.6）。
     */
    List<SupplierChangeRequest> findPendingBySupplierId(Long supplierId);

    /**
     * 按状态查询（采购端待审核变更列表，Req 5.1）。
     */
    List<SupplierChangeRequest> findByStatus(ChangeRequestStatus status);

    /**
     * 提交早于阈值且仍待审核的变更（24h 未审核再提醒，Req 5.8）。
     */
    List<SupplierChangeRequest> findPendingSubmittedBefore(LocalDateTime threshold);
}
