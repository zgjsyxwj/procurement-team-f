package com.cdp.ecosaas.procurement.supplier.domain.model;

import lombok.Getter;

/**
 * 供应商信息变更类型枚举（Req 3.3、3.6）。
 * <p>
 * 同一供应商同一 {@code ChangeType} 至多存在一条待审核变更。
 */
@Getter
public enum ChangeType {

    BASIC_INFO("基本信息"),
    BANK("银行信息");

    private final String description;

    ChangeType(String description) {
        this.description = description;
    }
}
