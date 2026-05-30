package com.cdp.ecosaas.procurement.supplier.infrastructure.external;

import com.cdp.ecosaas.procurement.auth.infrastructure.persistence.entity.BuyerSupplierRelationEntity;
import com.cdp.ecosaas.procurement.auth.infrastructure.persistence.repository.JpaBuyerSupplierRelationRepository;
import com.cdp.ecosaas.procurement.shared.util.SecurityUtils;
import com.cdp.ecosaas.procurement.supplier.domain.port.BuyerSupplierRelationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 采购员-供应商管理关系适配器 —— {@link BuyerSupplierRelationPort} 的实现。
 * <p>
 * 复用模块 01 现有的 {@link JpaBuyerSupplierRelationRepository}（落 {@code auth_buyer_supplier_relation} 表），
 * 不在模块 01 新增代码（任务 6.2）。用于创建供应商时建立管理关系（Req 6.4），
 * 以及数据范围隔离（Req 2.12、50.5）与变更通知收件人查询（Req 3.4、5.4）。
 */
@Component
@RequiredArgsConstructor
public class BuyerSupplierRelationAdapter implements BuyerSupplierRelationPort {

    private final JpaBuyerSupplierRelationRepository relationRepository;

    @Override
    public void createRelation(Long buyerId, Long supplierId, String source) {
        Long operatorId = SecurityUtils.getCurrentUserId();
        BuyerSupplierRelationEntity entity = BuyerSupplierRelationEntity.builder()
                .buyerId(buyerId)
                .supplierId(supplierId)
                .source(source)
                .createdAt(LocalDateTime.now())
                .createdBy(operatorId == null ? null : String.valueOf(operatorId))
                .build();
        relationRepository.save(entity);
    }

    @Override
    public List<Long> findSupplierIdsByBuyer(Long buyerId) {
        return relationRepository.findByBuyerId(buyerId).stream()
                .map(BuyerSupplierRelationEntity::getSupplierId)
                .toList();
    }

    @Override
    public List<Long> findBuyerIdsBySupplier(Long supplierId) {
        return relationRepository.findBySupplierId(supplierId).stream()
                .map(BuyerSupplierRelationEntity::getBuyerId)
                .toList();
    }
}
