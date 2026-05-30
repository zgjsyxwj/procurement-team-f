package com.cdp.ecosaas.procurement.supplier.domain.service;

import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierContact;
import com.cdp.ecosaas.procurement.supplier.shared.exception.PrimaryContactRequiredException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ContactDomainService 单元测试 —— 主要联系人唯一性与删除约束（Req 9.3、9.4、9.5）。
 */
class ContactDomainServiceTests {

    private final ContactDomainService service = new ContactDomainService();

    private SupplierContact contact(Long id, boolean primary) {
        return SupplierContact.builder()
                .id(id)
                .supplierId(1L)
                .name("联系人" + id)
                .phone("13800138000")
                .email("c" + id + "@example.com")
                .isPrimary(primary)
                .build();
    }

    @Test
    @DisplayName("setPrimary 应将目标设为主要并取消其他")
    void shouldSetPrimaryAndUnmarkOthers() {
        SupplierContact c1 = contact(1L, true);
        SupplierContact c2 = contact(2L, false);

        service.setPrimary(List.of(c1, c2), 2L);

        assertFalse(c1.isPrimary());
        assertTrue(c2.isPrimary());
    }

    @Test
    @DisplayName("setPrimary 指定不存在的联系人应抛异常")
    void shouldThrowWhenTargetNotFound() {
        assertThrows(IllegalArgumentException.class,
                () -> service.setPrimary(List.of(contact(1L, true)), 99L));
    }

    @Test
    @DisplayName("不可删除主要联系人（需先指定其他为主要）")
    void shouldRejectDeletingPrimary() {
        List<SupplierContact> contacts = List.of(contact(1L, true), contact(2L, false));

        assertThrows(PrimaryContactRequiredException.class,
                () -> service.ensureCanDelete(contacts, 1L));
    }

    @Test
    @DisplayName("可删除非主要联系人")
    void shouldAllowDeletingNonPrimary() {
        List<SupplierContact> contacts = List.of(contact(1L, true), contact(2L, false));

        assertDoesNotThrow(() -> service.ensureCanDelete(contacts, 2L));
    }

    @Test
    @DisplayName("缺少主要联系人应抛异常")
    void shouldRequireAtLeastOnePrimary() {
        assertThrows(PrimaryContactRequiredException.class,
                () -> service.ensureHasPrimary(List.of(contact(1L, false))));
    }
}
