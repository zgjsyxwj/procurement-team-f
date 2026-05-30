package com.cdp.ecosaas.procurement.supplier.shared.exception;

import com.cdp.ecosaas.procurement.shared.exception.BusinessException;

/**
 * 证件文件非法异常 —— 上传的证件文件不满足白名单约束（格式 PDF/JPG/PNG、大小 ≤100MB）时抛出（Req 10.6）。
 * <p>
 * 错误码：{@link SupplierErrorCode#INVALID_CERTIFICATE_FILE}。
 */
public class InvalidCertificateFileException extends BusinessException {

    public InvalidCertificateFileException(String detail) {
        super(SupplierErrorCode.INVALID_CERTIFICATE_FILE.getCode(),
              SupplierErrorCode.INVALID_CERTIFICATE_FILE.getMessage(),
              detail);
    }
}
