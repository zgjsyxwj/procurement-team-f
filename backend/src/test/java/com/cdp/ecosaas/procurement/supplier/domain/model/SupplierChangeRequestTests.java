package com.cdp.ecosaas.procurement.supplier.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SupplierChangeRequest 聚合根单元测试 —— 审核通过/驳回/撤回流转（Req 3.7、5.3、5.4）。
 */
class SupplierChangeRequestTests {

    private SupplierChangeRequest pendingRequest() {
        return SupplierChangeRequest.builder()
                .id(1L)
                .supplierId(1L)
                .changeType(ChangeType.BASIC_INFO)
                .source(ChangeSource.SUPPLIER)
                .status(ChangeRequestStatus.PENDING_REVIEW)
                .submitterId(10L)
                .submitterName("供应商联系人")
                .submittedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("approve / reject / withdraw - 合法流转")
    class LegalTransitions {

        @Test
        @DisplayName("审核通过应置为已通过并记录审核人与时间")
        void shouldApprovePendingRequest() {
            SupplierChangeRequest request = pendingRequest();

            request.approve(20L, "采购员张三");

            assertEquals(ChangeRequestStatus.APPROVED, request.getStatus());
            assertEquals(20L, request.getReviewerId());
            assertEquals("采购员张三", request.getReviewerName());
            assertNotNull(request.getReviewedAt());
        }

        @Test
        @DisplayName("审核驳回应置为驳回并记录原因")
        void shouldRejectPendingRequestWithReason() {
            SupplierChangeRequest request = pendingRequest();

            request.reject(20L, "采购员张三", "注册资金填写有误");

            assertEquals(ChangeRequestStatus.REJECTED, request.getStatus());
            assertEquals("注册资金填写有误", request.getReviewComment());
            assertNotNull(request.getReviewedAt());
        }

        @Test
        @DisplayName("撤回应置为已撤回并记录撤回时间")
        void shouldWithdrawPendingRequest() {
            SupplierChangeRequest request = pendingRequest();

            request.withdraw();

            assertEquals(ChangeRequestStatus.WITHDRAWN, request.getStatus());
            assertNotNull(request.getWithdrawnAt());
        }
    }

    @Nested
    @DisplayName("非法流转抛 IllegalStateException")
    class IllegalTransitions {

        @Test
        @DisplayName("非待审核状态不可审核通过")
        void shouldRejectApproveWhenNotPending() {
            SupplierChangeRequest request = pendingRequest();
            request.approve(20L, "采购员张三");

            assertThrows(IllegalStateException.class, () -> request.approve(20L, "采购员张三"));
        }

        @Test
        @DisplayName("非待审核状态不可撤回")
        void shouldRejectWithdrawWhenNotPending() {
            SupplierChangeRequest request = pendingRequest();
            request.reject(20L, "采购员张三", "原因");

            assertThrows(IllegalStateException.class, request::withdraw);
        }
    }
}
