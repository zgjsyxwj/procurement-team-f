package com.cdp.ecosaas.procurement.supplier.shared.exception;

import com.cdp.ecosaas.procurement.shared.exception.BusinessException;

/**
 * 供应商不存在异常 —— 按 ID 找不到供应商时抛出。
 * <p>
 * 错误码：{@link SupplierErrorCode#SUPPLIER_NOT_FOUND}。
 */
public class SupplierNotFoundException extends BusinessException {

    public SupplierNotFoundException(Long supplierId) {
        super(SupplierErrorCode.SUPPLIER_NOT_FOUND.getCode(),
              SupplierErrorCode.SUPPLIER_NOT_FOUND.getMessage(),
              "供应商不存在：" + supplierId);
    }
}
