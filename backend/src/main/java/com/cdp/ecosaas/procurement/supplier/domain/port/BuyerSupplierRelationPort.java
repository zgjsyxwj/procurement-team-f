package com.cdp.ecosaas.procurement.supplier.domain.port;

import java.util.List;

/**
 * 采购员-供应商管理关系端口（出站，对接模块 01）。
 * <p>
 * 用于建立创建供应商时的管理关系，以及数据范围隔离与变更通知收件人查询。
 * 实现见 {@code BuyerSupplierRelationAdapter}（任务 6.2），落到模块 01 {@code auth_buyer_supplier_relation}。
 */
public interface BuyerSupplierRelationPort {

    /**
     * 建立采购员-供应商管理关系（Req 6.4）。
     *
     * @param source 关系来源，如 {@code CREATED}/{@code PR_ASSIGNED}/{@code MANUAL}
     */
    void createRelation(Long buyerId, Long supplierId, String source);

    /**
     * 查询某采购员管理的供应商 ID 集合（数据范围隔离，Req 2.12、50.5）。
     */
    List<Long> findSupplierIdsByBuyer(Long buyerId);

    /**
     * 查询管理某供应商的采购员 ID 集合（变更待审核/审核结果通知收件人，Req 3.4、5.4）。
     */
    List<Long> findBuyerIdsBySupplier(Long supplierId);
}
