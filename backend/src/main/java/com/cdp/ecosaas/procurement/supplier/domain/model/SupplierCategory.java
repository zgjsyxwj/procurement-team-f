package com.cdp.ecosaas.procurement.supplier.domain.model;

import lombok.Getter;

/**
 * 供应商分类枚举（Req 6.1）。
 */
@Getter
public enum SupplierCategory {

    DOMESTIC("国内"),
    OVERSEAS("国外");

    private final String description;

    SupplierCategory(String description) {
        this.description = description;
    }
}
