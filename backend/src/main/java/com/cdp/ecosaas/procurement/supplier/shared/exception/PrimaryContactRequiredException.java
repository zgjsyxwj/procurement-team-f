package com.cdp.ecosaas.procurement.supplier.shared.exception;

import com.cdp.ecosaas.procurement.shared.exception.BusinessException;

/**
 * 主要联系人约束异常 —— 删除唯一主要联系人或供应商缺少主要联系人时抛出（Req 9.3、9.5）。
 * <p>
 * 错误码：{@link SupplierErrorCode#PRIMARY_CONTACT_REQUIRED}。
 */
public class PrimaryContactRequiredException extends BusinessException {

    public PrimaryContactRequiredException(String detail) {
        super(SupplierErrorCode.PRIMARY_CONTACT_REQUIRED.getCode(),
              SupplierErrorCode.PRIMARY_CONTACT_REQUIRED.getMessage(),
              detail);
    }
}
