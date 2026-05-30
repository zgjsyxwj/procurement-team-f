package com.cdp.ecosaas.procurement.supplier.application.service;

import com.cdp.ecosaas.procurement.supplier.domain.port.BuyerSupplierRelationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 供应商数据范围服务（Req 2.12、50.5）—— 按角色与采购员-供应商管理关系裁剪可见供应商。
 * <p>
 * 角色与用户 ID 由调用方（Controller，经 {@code SecurityUtils}）传入，保持本服务无框架依赖、可单测。
 * 采购端供应商列表/审核中心仅 ADMIN/BUYER 使用；供应商端仅见本企业，由各自接口直接按 supplierId 限定。
 */
@Service
@RequiredArgsConstructor
public class SupplierAccessService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_BUYER = "BUYER";

    private final BuyerSupplierRelationPort relationPort;

    /**
     * 当前用户可见的供应商 ID 范围。
     *
     * @return {@code null} 表示不受限（ADMIN 全量）；否则为可见供应商 ID 列表（BUYER 管理范围；其他角色为空）。
     */
    public List<Long> accessibleSupplierIds(String role, Long userId) {
        if (ROLE_ADMIN.equals(role)) {
            return null;
        }
        if (ROLE_BUYER.equals(role)) {
            return relationPort.findSupplierIdsByBuyer(userId);
        }
        return List.of();
    }

    /**
     * 当前用户是否可访问指定供应商。
     */
    public boolean canAccess(String role, Long userId, Long supplierId) {
        List<Long> accessible = accessibleSupplierIds(role, userId);
        return accessible == null || accessible.contains(supplierId);
    }
}
