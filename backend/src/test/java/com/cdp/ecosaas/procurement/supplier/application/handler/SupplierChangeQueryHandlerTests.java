package com.cdp.ecosaas.procurement.supplier.application.handler;

import com.cdp.ecosaas.procurement.shared.exception.BusinessException;
import com.cdp.ecosaas.procurement.supplier.application.service.SupplierAccessService;
import com.cdp.ecosaas.procurement.supplier.domain.model.ChangeRequestStatus;
import com.cdp.ecosaas.procurement.supplier.domain.model.ChangeType;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierChangeRequest;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierChangeRequestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * SupplierChangeQueryHandler 单元测试 —— 待审核列表（数据范围）、详情、变更历史（Req 5.1、5.2、50.2）。
 */
class SupplierChangeQueryHandlerTests {

    private final SupplierChangeRequestRepository changeRequestRepository =
            mock(SupplierChangeRequestRepository.class);
    private final SupplierAccessService accessService = mock(SupplierAccessService.class);

    private final SupplierChangeQueryHandler handler =
            new SupplierChangeQueryHandler(changeRequestRepository, accessService);

    private SupplierChangeRequest pending(Long id, Long supplierId) {
        return SupplierChangeRequest.builder().id(id).supplierId(supplierId)
                .changeType(ChangeType.BASIC_INFO).status(ChangeRequestStatus.PENDING_REVIEW).build();
    }

    @Test
    @DisplayName("ADMIN 待审核列表：返回全部待审核变更")
    void shouldReturnAllPendingForAdmin() {
        when(changeRequestRepository.findByStatus(ChangeRequestStatus.PENDING_REVIEW))
                .thenReturn(List.of(pending(1L, 10L), pending(2L, 20L)));
        when(accessService.accessibleSupplierIds("ADMIN", 1L)).thenReturn(null);

        assertEquals(2, handler.findPendingChanges("ADMIN", 1L).size());
    }

    @Test
    @DisplayName("BUYER 待审核列表：仅保留管理范围内供应商的变更")
    void shouldFilterPendingByBuyerScope() {
        when(changeRequestRepository.findByStatus(ChangeRequestStatus.PENDING_REVIEW))
                .thenReturn(List.of(pending(1L, 10L), pending(2L, 20L)));
        when(accessService.accessibleSupplierIds("BUYER", 7L)).thenReturn(List.of(10L));

        List<SupplierChangeRequest> result = handler.findPendingChanges("BUYER", 7L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getSupplierId());
    }

    @Test
    @DisplayName("变更详情不存在抛业务异常")
    void shouldThrowWhenChangeDetailNotFound() {
        when(changeRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> handler.findDetail(99L));
    }

    @Test
    @DisplayName("变更历史：委托仓储按供应商查询")
    void shouldReturnChangeHistory() {
        when(changeRequestRepository.findBySupplierId(10L))
                .thenReturn(List.of(pending(1L, 10L), pending(3L, 10L)));

        assertEquals(2, handler.findChangeHistory(10L).size());
    }
}
