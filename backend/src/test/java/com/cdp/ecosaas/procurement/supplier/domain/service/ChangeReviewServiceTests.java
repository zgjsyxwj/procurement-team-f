package com.cdp.ecosaas.procurement.supplier.domain.service;

import com.cdp.ecosaas.procurement.supplier.domain.model.ChangeRequestStatus;
import com.cdp.ecosaas.procurement.supplier.domain.model.ChangeType;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierChangeField;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierChangeRequest;
import com.cdp.ecosaas.procurement.supplier.shared.exception.DuplicatePendingChangeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ChangeReviewService 单元测试 —— 字段差异计算 + 同类待审核冲突拦截（Req 3.3、3.6、5.2）。
 */
class ChangeReviewServiceTests {

    private final ChangeReviewService service = new ChangeReviewService();

    @Test
    @DisplayName("computeChangedFields 仅返回发生变化的字段")
    void shouldReturnOnlyChangedFields() {
        Map<String, String> before = Map.of("name", "旧名", "capital", "100");
        Map<String, String> after = Map.of("name", "新名", "capital", "100");
        Map<String, String> labels = Map.of("name", "供应商名称", "capital", "注册资金");

        List<SupplierChangeField> changes = service.computeChangedFields(before, after, labels);

        assertEquals(1, changes.size());
        SupplierChangeField change = changes.get(0);
        assertEquals("name", change.getFieldKey());
        assertEquals("供应商名称", change.getFieldLabel());
        assertEquals("旧名", change.getBeforeValue());
        assertEquals("新名", change.getAfterValue());
    }

    @Test
    @DisplayName("无变化时返回空列表")
    void shouldReturnEmptyWhenNoChange() {
        Map<String, String> same = Map.of("name", "X");

        assertTrue(service.computeChangedFields(same, same, Map.of("name", "名称")).isEmpty());
    }

    @Test
    @DisplayName("已存在同类待审核变更应抛 DuplicatePendingChangeException")
    void shouldRejectWhenPendingSameTypeExists() {
        List<SupplierChangeRequest> existing = List.of(
                SupplierChangeRequest.builder()
                        .changeType(ChangeType.BASIC_INFO)
                        .status(ChangeRequestStatus.PENDING_REVIEW)
                        .build());

        assertThrows(DuplicatePendingChangeException.class,
                () -> service.ensureNoPendingConflict(existing, ChangeType.BASIC_INFO));
    }

    @Test
    @DisplayName("无同类待审核变更时通过（不同类型或已审核不冲突）")
    void shouldPassWhenNoPendingSameType() {
        List<SupplierChangeRequest> existing = List.of(
                SupplierChangeRequest.builder()
                        .changeType(ChangeType.BANK)
                        .status(ChangeRequestStatus.PENDING_REVIEW)
                        .build(),
                SupplierChangeRequest.builder()
                        .changeType(ChangeType.BASIC_INFO)
                        .status(ChangeRequestStatus.APPROVED)
                        .build());

        assertDoesNotThrow(() -> service.ensureNoPendingConflict(existing, ChangeType.BASIC_INFO));
    }
}
