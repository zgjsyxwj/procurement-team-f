package com.cdp.ecosaas.procurement.supplier.application.service;

import com.cdp.ecosaas.procurement.supplier.domain.port.BuyerSupplierRelationPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * SupplierAccessService 单元测试 —— 角色数据范围（Req 2.12、50.5）。
 */
class SupplierAccessServiceTests {

    private final BuyerSupplierRelationPort relationPort = mock(BuyerSupplierRelationPort.class);
    private final SupplierAccessService service = new SupplierAccessService(relationPort);

    @Test
    @DisplayName("ADMIN 不受限：返回 null，不查询管理关系")
    void shouldReturnUnrestrictedForAdmin() {
        assertNull(service.accessibleSupplierIds("ADMIN", 1L));
        assertTrue(service.canAccess("ADMIN", 1L, 999L));
        verifyNoInteractions(relationPort);
    }

    @Test
    @DisplayName("BUYER 仅可见其管理范围内的供应商")
    void shouldReturnManagedSuppliersForBuyer() {
        when(relationPort.findSupplierIdsByBuyer(7L)).thenReturn(List.of(10L, 20L));

        assertEquals(List.of(10L, 20L), service.accessibleSupplierIds("BUYER", 7L));
        assertTrue(service.canAccess("BUYER", 7L, 10L));
        assertFalse(service.canAccess("BUYER", 7L, 30L));
    }

    @Test
    @DisplayName("其他角色（如业务人员）无采购端数据范围：空清单")
    void shouldReturnEmptyForOtherRoles() {
        assertTrue(service.accessibleSupplierIds("BUSINESS_USER", 5L).isEmpty());
        assertFalse(service.canAccess("BUSINESS_USER", 5L, 10L));
        verifyNoInteractions(relationPort);
    }
}
