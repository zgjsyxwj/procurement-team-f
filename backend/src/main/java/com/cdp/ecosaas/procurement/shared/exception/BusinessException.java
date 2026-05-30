package com.cdp.ecosaas.procurement.shared.exception;

/**
 * 业务异常基类。
 * <p>
 * 所有模块的业务异常应继承此类，由全局异常处理器统一处理。
 * <p>
 * 错误码采用结构化格式 {@code {DOMAIN}.{CODE}}（如 {@code AUTH.1001}），
 * 同时附带人类可读的 {@code messageCode}（如 {@code AUTHENTICATION_FAILED}）用于日志与监控；
 * {@link #getMessage()} 返回详细描述（可面向用户）。
 */
public class BusinessException extends RuntimeException {

    /**
     * 通用未分类业务错误码（最后兜底）。
     */
    public static final String DEFAULT_CODE = "COMMON.0001";
    public static final String DEFAULT_MESSAGE_CODE = "BUSINESS_ERROR";

    private final String errorCode;
    private final String messageCode;

    public BusinessException(String detail) {
        this(DEFAULT_CODE, DEFAULT_MESSAGE_CODE, detail, null);
    }

    public BusinessException(String errorCode, String messageCode, String detail) {
        this(errorCode, messageCode, detail, null);
    }

    public BusinessException(String errorCode, String messageCode, String detail, Throwable cause) {
        super(detail, cause);
        this.errorCode = errorCode;
        this.messageCode = messageCode;
    }

    /**
     * 结构化错误码，如 {@code AUTH.1001}。
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 可读消息标识，如 {@code AUTHENTICATION_FAILED}，用于日志与告警检索。
     */
    public String getMessageCode() {
        return messageCode;
    }
}
