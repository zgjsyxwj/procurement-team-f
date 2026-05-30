package com.cdp.ecosaas.procurement.supplier.application.listener;

import com.cdp.ecosaas.procurement.shared.event.SupplierFirstLoginEvent;
import com.cdp.ecosaas.procurement.supplier.domain.model.Supplier;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierCategory;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierStatus;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierRepository;
import com.cdp.ecosaas.procurement.supplier.domain.service.SupplierLifecycleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SupplierFirstLoginListener 单元测试 —— 首登流转「创建成功/待进入 → 待完善信息」（Req 7.3）。
 */
class SupplierFirstLoginListenerTests {

    private final SupplierRepository supplierRepository = mock(SupplierRepository.class);
    private final SupplierFirstLoginListener listener =
            new SupplierFirstLoginListener(supplierRepository, new SupplierLifecycleService());

    private Supplier supplier(SupplierStatus status) {
        return Supplier.builder().id(1L).supplierCode("VD0001").name("测试供应商")
                .category(SupplierCategory.DOMESTIC).status(status).build();
    }

    @Test
    @DisplayName("待进入供应商首登：流转为待完善信息并保存")
    void shouldTransitionPendingEntryToPendingInfo() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier(SupplierStatus.PENDING_ENTRY)));
        when(supplierRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        listener.onSupplierFirstLogin(new SupplierFirstLoginEvent(1L));

        ArgumentCaptor<Supplier> captor = ArgumentCaptor.forClass(Supplier.class);
        verify(supplierRepository).save(captor.capture());
        assertEquals(SupplierStatus.PENDING_INFO, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("已越过状态（如合作中）的供应商首登事件被忽略，不保存")
    void shouldIgnoreWhenSupplierAlreadyBeyondInitialStates() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier(SupplierStatus.ACTIVE)));

        listener.onSupplierFirstLogin(new SupplierFirstLoginEvent(1L));

        verify(supplierRepository, never()).save(any());
    }

    @Test
    @DisplayName("供应商不存在时安全跳过，不抛异常、不保存")
    void shouldSkipSafelyWhenSupplierMissing() {
        when(supplierRepository.findById(99L)).thenReturn(Optional.empty());

        listener.onSupplierFirstLogin(new SupplierFirstLoginEvent(99L));

        verify(supplierRepository, never()).save(any());
    }
}
