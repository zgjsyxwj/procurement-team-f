package com.cdp.ecosaas.procurement.supplier.shared.exception;

import com.cdp.ecosaas.procurement.shared.exception.BusinessException;

/**
 * 重复待审核变更异常 —— 同一供应商同一变更类型已存在待审核变更时抛出（Req 3.6）。
 * <p>
 * 错误码：{@link SupplierErrorCode#DUPLICATE_PENDING_CHANGE}。
 */
public class DuplicatePendingChangeException extends BusinessException {

    public DuplicatePendingChangeException(String detail) {
        super(SupplierErrorCode.DUPLICATE_PENDING_CHANGE.getCode(),
              SupplierErrorCode.DUPLICATE_PENDING_CHANGE.getMessage(),
              detail);
    }
}
