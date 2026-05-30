package com.cdp.ecosaas.procurement.supplier.interfaces.dto;

import com.cdp.ecosaas.procurement.supplier.domain.model.Supplier;

/**
 * 合作中供应商响应（供模块 04 询报价选择）。
 */
public record ActiveSupplierResponse(Long id, String supplierCode, String name) {

    public static ActiveSupplierResponse from(Supplier supplier) {
        return new ActiveSupplierResponse(supplier.getId(), supplier.getSupplierCode(), supplier.getName());
    }
}
