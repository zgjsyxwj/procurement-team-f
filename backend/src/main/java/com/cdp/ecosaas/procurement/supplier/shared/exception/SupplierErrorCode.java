package com.cdp.ecosaas.procurement.supplier.shared.exception;

import lombok.Getter;

/**
 * 供应商模块错误码（结构化）。
 * <p>
 * 错误码格式 {@code {DOMAIN}.{CODE}}，与 auth 模块一致：
 * 1xxx 客户端错误（4xx），2xxx 服务端错误（5xx）。
 * 同时携带可读 {@code message} 标识（大写下划线）用于日志检索与监控。
 * <p>
 * 注：对应的异常类与 {@code GlobalExceptionHandler} 的 HTTP 状态映射在任务 17.1 统一完善；
 * 本阶段仅创建领域状态机所需的 {@link com.cdp.ecosaas.procurement.supplier.shared.exception.InvalidSupplierStatusException}。
 */
@Getter
public enum SupplierErrorCode {

    // ---------- 1xxx 客户端错误 ----------
    SUPPLIER_NOT_FOUND("SUPPLIER.1001", "SUPPLIER_NOT_FOUND"),
    INVALID_SUPPLIER_STATUS("SUPPLIER.1002", "INVALID_SUPPLIER_STATUS"),
    DUPLICATE_PENDING_CHANGE("SUPPLIER.1003", "DUPLICATE_PENDING_CHANGE"),
    PRIMARY_CONTACT_REQUIRED("SUPPLIER.1004", "PRIMARY_CONTACT_REQUIRED"),
    INVALID_CERTIFICATE_FILE("SUPPLIER.1005", "INVALID_CERTIFICATE_FILE");

    private final String code;
    private final String message;

    SupplierErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
