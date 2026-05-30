package com.cdp.ecosaas.procurement.supplier.domain.service;

import com.cdp.ecosaas.procurement.supplier.domain.model.Supplier;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierCategory;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierStatus;
import com.cdp.ecosaas.procurement.supplier.domain.port.SupplierAccountPort;
import com.cdp.ecosaas.procurement.supplier.shared.exception.InvalidSupplierStatusException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SupplierLifecycleService 单元测试 —— 集中流转 + 停用/启用联动账号端口（Req 7.2-7.11）。
 */
class SupplierLifecycleServiceTests {

    private final SupplierLifecycleService service = new SupplierLifecycleService();

    private Supplier supplier(SupplierStatus status) {
        return Supplier.builder()
                .id(1L)
                .supplierCode("VD0001")
                .name("测试供应商")
                .category(SupplierCategory.DOMESTIC)
                .status(status)
                .build();
    }

    @Test
    @DisplayName("disable 应停用供应商并同步停用模块01账号")
    void shouldDisableSupplierAndAccount() {
        SupplierAccountPort accountPort = mock(SupplierAccountPort.class);
        Supplier supplier = supplier(SupplierStatus.ACTIVE);

        service.disable(supplier, accountPort);

        assertEquals(SupplierStatus.DISABLED, supplier.getStatus());
        verify(accountPort).disableAccount(1L);
    }

    @Test
    @DisplayName("enable 应启用供应商并同步启用模块01账号")
    void shouldEnableSupplierAndAccount() {
        SupplierAccountPort accountPort = mock(SupplierAccountPort.class);
        Supplier supplier = supplier(SupplierStatus.DISABLED);

        service.enable(supplier, accountPort);

        assertEquals(SupplierStatus.ACTIVE, supplier.getStatus());
        verify(accountPort).enableAccount(1L);
    }

    @Test
    @DisplayName("非法停用应抛异常且不触碰账号端口")
    void shouldNotTouchAccountWhenDisableIllegal() {
        SupplierAccountPort accountPort = mock(SupplierAccountPort.class);
        Supplier supplier = supplier(SupplierStatus.DISABLED);

        assertThrows(InvalidSupplierStatusException.class, () -> service.disable(supplier, accountPort));
        verifyNoInteractions(accountPort);
    }

    @Test
    @DisplayName("approve 委托聚合根完成 待审核信息->合作中")
    void shouldApproveViaAggregate() {
        Supplier supplier = supplier(SupplierStatus.PENDING_REVIEW);

        service.approve(supplier);

        assertEquals(SupplierStatus.ACTIVE, supplier.getStatus());
    }

    @Test
    @DisplayName("invite 非法状态应抛异常")
    void shouldRejectInviteWhenIllegal() {
        Supplier supplier = supplier(SupplierStatus.ACTIVE);

        assertThrows(InvalidSupplierStatusException.class, () -> service.invite(supplier));
    }
}
