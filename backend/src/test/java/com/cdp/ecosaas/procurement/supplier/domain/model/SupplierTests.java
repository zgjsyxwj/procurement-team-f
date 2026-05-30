package com.cdp.ecosaas.procurement.supplier.domain.model;

import com.cdp.ecosaas.procurement.supplier.shared.exception.InvalidSupplierStatusException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Supplier 聚合根单元测试 —— 状态机、报价资格、银行信息完整性约束（Req 7、4.5、3.9）。
 */
class SupplierTests {

    private Supplier supplierWithStatus(SupplierStatus status) {
        return Supplier.builder()
                .id(1L)
                .supplierCode("VD0001")
                .name("测试供应商")
                .category(SupplierCategory.DOMESTIC)
                .status(status)
                .build();
    }

    @Nested
    @DisplayName("状态机 - 合法流转")
    class LegalTransitions {

        @Test
        @DisplayName("invite: 创建成功 -> 待进入")
        void shouldTransitionToPendingEntryOnInviteFromCreated() {
            Supplier supplier = supplierWithStatus(SupplierStatus.CREATED);

            supplier.invite();

            assertEquals(SupplierStatus.PENDING_ENTRY, supplier.getStatus());
        }

        @Test
        @DisplayName("invite: 待进入状态重发邀请仍为待进入")
        void shouldStayPendingEntryOnReInvite() {
            Supplier supplier = supplierWithStatus(SupplierStatus.PENDING_ENTRY);

            supplier.invite();

            assertEquals(SupplierStatus.PENDING_ENTRY, supplier.getStatus());
        }

        @Test
        @DisplayName("onFirstLogin: 待进入 -> 待完善信息")
        void shouldTransitionToPendingInfoOnFirstLoginFromPendingEntry() {
            Supplier supplier = supplierWithStatus(SupplierStatus.PENDING_ENTRY);

            supplier.onFirstLogin();

            assertEquals(SupplierStatus.PENDING_INFO, supplier.getStatus());
        }

        @Test
        @DisplayName("onFirstLogin: 创建成功 -> 待完善信息")
        void shouldTransitionToPendingInfoOnFirstLoginFromCreated() {
            Supplier supplier = supplierWithStatus(SupplierStatus.CREATED);

            supplier.onFirstLogin();

            assertEquals(SupplierStatus.PENDING_INFO, supplier.getStatus());
        }

        @Test
        @DisplayName("submitForReview: 待完善信息 -> 待审核信息")
        void shouldTransitionToPendingReviewOnSubmit() {
            Supplier supplier = supplierWithStatus(SupplierStatus.PENDING_INFO);

            supplier.submitForReview();

            assertEquals(SupplierStatus.PENDING_REVIEW, supplier.getStatus());
        }

        @Test
        @DisplayName("approve: 待审核信息 -> 合作中")
        void shouldTransitionToActiveOnApprove() {
            Supplier supplier = supplierWithStatus(SupplierStatus.PENDING_REVIEW);

            supplier.approve();

            assertEquals(SupplierStatus.ACTIVE, supplier.getStatus());
        }

        @Test
        @DisplayName("reject: 待审核信息 -> 待完善信息")
        void shouldTransitionToPendingInfoOnReject() {
            Supplier supplier = supplierWithStatus(SupplierStatus.PENDING_REVIEW);

            supplier.reject();

            assertEquals(SupplierStatus.PENDING_INFO, supplier.getStatus());
        }

        @Test
        @DisplayName("activate: 待完善信息 -> 合作中（手动设为合作中）")
        void shouldTransitionToActiveOnActivateFromPendingInfo() {
            Supplier supplier = supplierWithStatus(SupplierStatus.PENDING_INFO);

            supplier.activate();

            assertEquals(SupplierStatus.ACTIVE, supplier.getStatus());
        }

        @Test
        @DisplayName("activate: 待审核信息 -> 合作中（手动设为合作中）")
        void shouldTransitionToActiveOnActivateFromPendingReview() {
            Supplier supplier = supplierWithStatus(SupplierStatus.PENDING_REVIEW);

            supplier.activate();

            assertEquals(SupplierStatus.ACTIVE, supplier.getStatus());
        }

        @Test
        @DisplayName("disable: 合作中 -> 已停用")
        void shouldTransitionToDisabledOnDisableFromActive() {
            Supplier supplier = supplierWithStatus(SupplierStatus.ACTIVE);

            supplier.disable();

            assertEquals(SupplierStatus.DISABLED, supplier.getStatus());
        }

        @Test
        @DisplayName("disable: 创建成功 -> 已停用")
        void shouldTransitionToDisabledOnDisableFromCreated() {
            Supplier supplier = supplierWithStatus(SupplierStatus.CREATED);

            supplier.disable();

            assertEquals(SupplierStatus.DISABLED, supplier.getStatus());
        }

        @Test
        @DisplayName("enable: 已停用 -> 合作中（重新启用）")
        void shouldTransitionToActiveOnEnable() {
            Supplier supplier = supplierWithStatus(SupplierStatus.DISABLED);

            supplier.enable();

            assertEquals(SupplierStatus.ACTIVE, supplier.getStatus());
        }
    }

    @Nested
    @DisplayName("状态机 - 非法流转抛 InvalidSupplierStatusException")
    class IllegalTransitions {

        @Test
        @DisplayName("invite: 合作中不可发送邀请")
        void shouldRejectInviteFromActive() {
            Supplier supplier = supplierWithStatus(SupplierStatus.ACTIVE);

            assertThrows(InvalidSupplierStatusException.class, supplier::invite);
        }

        @Test
        @DisplayName("onFirstLogin: 合作中不可触发首登流转")
        void shouldRejectFirstLoginFromActive() {
            Supplier supplier = supplierWithStatus(SupplierStatus.ACTIVE);

            assertThrows(InvalidSupplierStatusException.class, supplier::onFirstLogin);
        }

        @Test
        @DisplayName("submitForReview: 创建成功不可提交审核")
        void shouldRejectSubmitFromCreated() {
            Supplier supplier = supplierWithStatus(SupplierStatus.CREATED);

            assertThrows(InvalidSupplierStatusException.class, supplier::submitForReview);
        }

        @Test
        @DisplayName("approve: 待完善信息不可审核通过")
        void shouldRejectApproveFromPendingInfo() {
            Supplier supplier = supplierWithStatus(SupplierStatus.PENDING_INFO);

            assertThrows(InvalidSupplierStatusException.class, supplier::approve);
        }

        @Test
        @DisplayName("reject: 合作中不可驳回")
        void shouldRejectRejectFromActive() {
            Supplier supplier = supplierWithStatus(SupplierStatus.ACTIVE);

            assertThrows(InvalidSupplierStatusException.class, supplier::reject);
        }

        @Test
        @DisplayName("activate: 创建成功不可手动设为合作中")
        void shouldRejectActivateFromCreated() {
            Supplier supplier = supplierWithStatus(SupplierStatus.CREATED);

            assertThrows(InvalidSupplierStatusException.class, supplier::activate);
        }

        @Test
        @DisplayName("disable: 已停用不可再次停用")
        void shouldRejectDisableFromDisabled() {
            Supplier supplier = supplierWithStatus(SupplierStatus.DISABLED);

            assertThrows(InvalidSupplierStatusException.class, supplier::disable);
        }

        @Test
        @DisplayName("enable: 合作中不可重新启用")
        void shouldRejectEnableFromActive() {
            Supplier supplier = supplierWithStatus(SupplierStatus.ACTIVE);

            assertThrows(InvalidSupplierStatusException.class, supplier::enable);
        }
    }

    @Nested
    @DisplayName("canQuote - 报价资格")
    class CanQuoteTests {

        @Test
        @DisplayName("合作中可参与报价")
        void shouldAllowQuoteWhenActive() {
            Supplier supplier = supplierWithStatus(SupplierStatus.ACTIVE);

            assertTrue(supplier.canQuote());
        }

        @Test
        @DisplayName("非合作中不可参与报价")
        void shouldRejectQuoteWhenNotActive() {
            assertFalse(supplierWithStatus(SupplierStatus.PENDING_REVIEW).canQuote());
            assertFalse(supplierWithStatus(SupplierStatus.DISABLED).canQuote());
            assertFalse(supplierWithStatus(SupplierStatus.PENDING_INFO).canQuote());
        }
    }

    @Nested
    @DisplayName("SupplierBankAccount - 银行信息完整性（Req 3.9）")
    class BankAccountTests {

        @Test
        @DisplayName("三项齐全应创建成功且 isComplete 为真")
        void shouldCreateCompleteBankAccount() {
            SupplierBankAccount account = SupplierBankAccount.of("测试户名", "招商银行", "6225000011112222");

            assertTrue(account.isComplete());
        }

        @Test
        @DisplayName("仅填写部分字段应抛出异常")
        void shouldThrowWhenPartiallyFilled() {
            assertThrows(IllegalArgumentException.class,
                    () -> SupplierBankAccount.of("测试户名", "", "6225000011112222"));
            assertThrows(IllegalArgumentException.class,
                    () -> SupplierBankAccount.of(null, "招商银行", "6225000011112222"));
        }
    }
}
