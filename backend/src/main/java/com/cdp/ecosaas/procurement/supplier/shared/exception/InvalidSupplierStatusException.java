package com.cdp.ecosaas.procurement.supplier.shared.exception;

import com.cdp.ecosaas.procurement.shared.exception.BusinessException;

/**
 * 供应商状态非法流转异常 —— 当对供应商执行其当前状态不允许的状态流转时抛出（Req 7）。
 * <p>
 * 错误码：{@link SupplierErrorCode#INVALID_SUPPLIER_STATUS}。
 */
public class InvalidSupplierStatusException extends BusinessException {

    public InvalidSupplierStatusException(String detail) {
        super(SupplierErrorCode.INVALID_SUPPLIER_STATUS.getCode(),
              SupplierErrorCode.INVALID_SUPPLIER_STATUS.getMessage(),
              detail);
    }
}
