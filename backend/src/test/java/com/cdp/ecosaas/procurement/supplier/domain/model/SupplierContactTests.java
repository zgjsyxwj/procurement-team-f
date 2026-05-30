package com.cdp.ecosaas.procurement.supplier.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SupplierContact 实体单元测试 —— 主要联系人标记（Req 9.4）。
 * 跨联系人的"唯一主要联系人"约束由 ContactDomainService（任务 3.5）负责。
 */
class SupplierContactTests {

    private SupplierContact contact(boolean primary) {
        return SupplierContact.builder()
                .id(1L)
                .supplierId(1L)
                .name("张三")
                .phone("13800138000")
                .email("zhangsan@example.com")
                .isPrimary(primary)
                .build();
    }

    @Test
    @DisplayName("markPrimary 应标记为主要联系人")
    void shouldMarkAsPrimary() {
        SupplierContact contact = contact(false);

        contact.markPrimary();

        assertTrue(contact.isPrimary());
    }

    @Test
    @DisplayName("unmarkPrimary 应取消主要联系人标记")
    void shouldUnmarkPrimary() {
        SupplierContact contact = contact(true);

        contact.unmarkPrimary();

        assertFalse(contact.isPrimary());
    }
}
