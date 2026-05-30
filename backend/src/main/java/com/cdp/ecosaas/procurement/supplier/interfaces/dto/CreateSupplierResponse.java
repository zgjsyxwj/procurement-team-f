package com.cdp.ecosaas.procurement.supplier.interfaces.dto;

import com.cdp.ecosaas.procurement.supplier.domain.model.Supplier;

/**
 * 创建供应商响应（供应商 ID 与编号）。
 */
public record CreateSupplierResponse(Long id, String supplierCode) {

    public static CreateSupplierResponse from(Supplier supplier) {
        return new CreateSupplierResponse(supplier.getId(), supplier.getSupplierCode());
    }
}
