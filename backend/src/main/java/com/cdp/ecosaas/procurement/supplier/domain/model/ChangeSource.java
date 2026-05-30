package com.cdp.ecosaas.procurement.supplier.domain.model;

import lombok.Getter;

/**
 * 变更来源枚举（Req 3.3、49.3）。
 * <p>
 * {@code SUPPLIER} 表示供应商提交（需审核）；{@code BUYER} 表示采购员直接编辑（即时生效）。
 */
@Getter
public enum ChangeSource {

    SUPPLIER("供应商提交"),
    BUYER("采购员直接编辑");

    private final String description;

    ChangeSource(String description) {
        this.description = description;
    }
}
