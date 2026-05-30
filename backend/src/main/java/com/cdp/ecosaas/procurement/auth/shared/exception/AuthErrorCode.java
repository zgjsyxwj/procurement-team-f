package com.cdp.ecosaas.procurement.auth.shared.exception;

import lombok.Getter;

/**
 * 认证模块错误码（结构化）。
 * <p>
 * 错误码格式：{@code {DOMAIN}.{CODE}}，遵循 backend_spec §5.4：
 * <ul>
 *   <li>1000~1999：客户端错误（4xx）</li>
 *   <li>2000~2999：服务端错误（5xx）</li>
 *   <li>3000~3999：第三方错误</li>
 * </ul>
 * 同时携带可读的 {@code message} 标识符（大写下划线），用于日志检索与监控指标。
 */
@Getter
public enum AuthErrorCode {

    // ---------- 1xxx 客户端错误 ----------
    AUTHENTICATION_FAILED("AUTH.1001", "AUTHENTICATION_FAILED"),
    ACCOUNT_LOCKED("AUTH.1002", "ACCOUNT_LOCKED"),
    ACCOUNT_DISABLED("AUTH.1003", "ACCOUNT_DISABLED"),
    PASSWORD_POLICY_VIOLATION("AUTH.1004", "PASSWORD_POLICY_VIOLATION"),
    PASSWORD_HISTORY_VIOLATION("AUTH.1005", "PASSWORD_HISTORY_VIOLATION"),
    RESET_TOKEN_INVALID("AUTH.1006", "RESET_TOKEN_INVALID"),
    RESET_TOKEN_EXPIRED("AUTH.1007", "RESET_TOKEN_EXPIRED"),
    RESET_TOKEN_USED("AUTH.1008", "RESET_TOKEN_USED"),
    USER_NOT_FOUND("AUTH.1009", "USER_NOT_FOUND"),
    PHONE_ALREADY_USED("AUTH.1010", "PHONE_ALREADY_USED"),
    EMAIL_ALREADY_USED("AUTH.1011", "EMAIL_ALREADY_USED"),
    SUPER_ADMIN_IMMUTABLE("AUTH.1012", "SUPER_ADMIN_IMMUTABLE"),
    UNSUPPORTED_USER_TYPE("AUTH.1013", "UNSUPPORTED_USER_TYPE"),
    INVALID_SAML_RESPONSE("AUTH.1014", "INVALID_SAML_RESPONSE"),
    CSRF_TOKEN_MISMATCH("AUTH.1015", "CSRF_TOKEN_MISMATCH"),

    // ---------- 2xxx 服务端错误 ----------
    INTERNAL_AUTH_ERROR("AUTH.2001", "INTERNAL_AUTH_ERROR");

    private final String code;
    private final String message;

    AuthErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
