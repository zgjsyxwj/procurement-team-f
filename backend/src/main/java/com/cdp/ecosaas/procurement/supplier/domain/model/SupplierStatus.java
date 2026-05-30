package com.cdp.ecosaas.procurement.supplier.domain.model;

import lombok.Getter;

/**
 * 供应商生命周期状态枚举。
 * <p>
 * 状态流转由 {@link Supplier} 聚合根集中控制，非法流转抛出
 * {@code InvalidSupplierStatusException}（Req 7.1）。
 */
@Getter
public enum SupplierStatus {

    CREATED("创建成功"),
    PENDING_ENTRY("待进入"),
    PENDING_INFO("待完善信息"),
    PENDING_REVIEW("待审核信息"),
    ACTIVE("合作中"),
    DISABLED("已停用");

    private final String description;

    SupplierStatus(String description) {
        this.description = description;
    }
}
