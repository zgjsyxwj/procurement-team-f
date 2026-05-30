package com.cdp.ecosaas.procurement.supplier.application.handler;

import com.cdp.ecosaas.procurement.shared.exception.BusinessException;
import com.cdp.ecosaas.procurement.supplier.application.service.SupplierAccessService;
import com.cdp.ecosaas.procurement.supplier.domain.model.ChangeRequestStatus;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierChangeRequest;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierChangeRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 供应商变更查询处理器（任务 8.8）—— 审核中心待审核变更列表、变更详情、变更历史。
 * <p>
 * 待审核列表按数据范围裁剪（ADMIN 全量 / BUYER 管理范围，Req 5.1）；变更历史时间倒序由仓储保证（Req 50.2）。
 * 变更历史时间范围筛选（Req 50.3）暂未实现。
 */
@Service
@RequiredArgsConstructor
public class SupplierChangeQueryHandler {

    private final SupplierChangeRequestRepository changeRequestRepository;
    private final SupplierAccessService accessService;

    /** 待审核变更列表，按当前用户数据范围裁剪（Req 5.1）。 */
    public List<SupplierChangeRequest> findPendingChanges(String role, Long userId) {
        List<SupplierChangeRequest> pending =
                changeRequestRepository.findByStatus(ChangeRequestStatus.PENDING_REVIEW);
        List<Long> accessibleIds = accessService.accessibleSupplierIds(role, userId);
        if (accessibleIds == null) {
            return pending;
        }
        return pending.stream()
                .filter(request -> accessibleIds.contains(request.getSupplierId()))
                .toList();
    }

    /** 变更详情（前后对比，Req 5.2）。 */
    public SupplierChangeRequest findDetail(Long changeRequestId) {
        return changeRequestRepository.findById(changeRequestId)
                .orElseThrow(() -> new BusinessException("变更不存在：" + changeRequestId));
    }

    /** 某供应商的变更历史，时间倒序（Req 50.2）。 */
    public List<SupplierChangeRequest> findChangeHistory(Long supplierId) {
        return changeRequestRepository.findBySupplierId(supplierId);
    }

    /** 某供应商当前的待审核变更（供应商端查看/撤回入口，Req 3.6）。 */
    public List<SupplierChangeRequest> findPendingBySupplier(Long supplierId) {
        return changeRequestRepository.findPendingBySupplierId(supplierId);
    }
}
