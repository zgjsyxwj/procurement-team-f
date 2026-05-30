package com.cdp.ecosaas.procurement.supplier.infrastructure.external;

import com.cdp.ecosaas.procurement.auth.infrastructure.persistence.entity.BuyerSupplierRelationEntity;
import com.cdp.ecosaas.procurement.auth.infrastructure.persistence.repository.JpaBuyerSupplierRelationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * BuyerSupplierRelationAdapter 单元测试 —— 复用模块 01 关系仓储建立/查询管理关系
 * （Req 6.4、2.12、50.5、3.4、5.4）。
 */
class BuyerSupplierRelationAdapterTests {

    private final JpaBuyerSupplierRelationRepository relationRepository =
            mock(JpaBuyerSupplierRelationRepository.class);
    private final BuyerSupplierRelationAdapter adapter =
            new BuyerSupplierRelationAdapter(relationRepository);

    @Test
    @DisplayName("createRelation 构造并保存关系实体（buyerId/supplierId/source）")
    void shouldBuildAndSaveRelation() {
        adapter.createRelation(7L, 101L, "CREATED");

        ArgumentCaptor<BuyerSupplierRelationEntity> captor =
                ArgumentCaptor.forClass(BuyerSupplierRelationEntity.class);
        verify(relationRepository).save(captor.capture());
        BuyerSupplierRelationEntity saved = captor.getValue();
        assertEquals(7L, saved.getBuyerId());
        assertEquals(101L, saved.getSupplierId());
        assertEquals("CREATED", saved.getSource());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    @DisplayName("findSupplierIdsByBuyer 将关系映射为供应商ID列表")
    void shouldMapSupplierIdsByBuyer() {
        when(relationRepository.findByBuyerId(7L)).thenReturn(List.of(
                BuyerSupplierRelationEntity.builder().buyerId(7L).supplierId(101L).build(),
                BuyerSupplierRelationEntity.builder().buyerId(7L).supplierId(102L).build()));

        assertEquals(List.of(101L, 102L), adapter.findSupplierIdsByBuyer(7L));
    }

    @Test
    @DisplayName("findBuyerIdsBySupplier 将关系映射为采购员ID列表")
    void shouldMapBuyerIdsBySupplier() {
        when(relationRepository.findBySupplierId(101L)).thenReturn(List.of(
                BuyerSupplierRelationEntity.builder().buyerId(7L).supplierId(101L).build(),
                BuyerSupplierRelationEntity.builder().buyerId(8L).supplierId(101L).build()));

        assertEquals(List.of(7L, 8L), adapter.findBuyerIdsBySupplier(101L));
    }
}
