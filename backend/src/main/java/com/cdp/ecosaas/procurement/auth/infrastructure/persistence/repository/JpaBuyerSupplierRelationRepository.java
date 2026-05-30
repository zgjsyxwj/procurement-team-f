package com.cdp.ecosaas.procurement.auth.infrastructure.persistence.repository;

import com.cdp.ecosaas.procurement.auth.infrastructure.persistence.entity.BuyerSupplierRelationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 采购员-供应商关系仓储实现。
 * <p>
 * 提供按 buyerId 和 supplierId 查询关系的功能。
 */
@Repository
@RequiredArgsConstructor
public class JpaBuyerSupplierRelationRepository {

    private final BuyerSupplierRelationJpaDao jpaDao;

    /**
     * 按采购员ID和供应商ID查询关系。
     *
     * @param buyerId    采购员ID
     * @param supplierId 供应商ID
     * @return 关系实体（如果存在）
     */
    public Optional<BuyerSupplierRelationEntity> findByBuyerIdAndSupplierId(Long buyerId, Long supplierId) {
        return jpaDao.findByBuyerIdAndSupplierId(buyerId, supplierId);
    }

    /**
     * 按采购员ID查询所有关系。
     *
     * @param buyerId 采购员ID
     * @return 关系列表
     */
    public List<BuyerSupplierRelationEntity> findByBuyerId(Long buyerId) {
        return jpaDao.findByBuyerId(buyerId);
    }

    /**
     * 按供应商ID查询所有关系。
     *
     * @param supplierId 供应商ID
     * @return 关系列表
     */
    public List<BuyerSupplierRelationEntity> findBySupplierId(Long supplierId) {
        return jpaDao.findBySupplierId(supplierId);
    }

    /**
     * 保存采购员-供应商关系。
     *
     * @param entity 关系实体
     * @return 保存后的实体
     */
    public BuyerSupplierRelationEntity save(BuyerSupplierRelationEntity entity) {
        return jpaDao.save(entity);
    }
}
