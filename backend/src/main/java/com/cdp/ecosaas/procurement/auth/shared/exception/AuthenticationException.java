package com.cdp.ecosaas.procurement.auth.shared.exception;

import com.cdp.ecosaas.procurement.shared.exception.BusinessException;

/**
 * 认证异常 - 用户凭据验证失败时抛出。
 * <p>
 * 错误码：{@link AuthErrorCode#AUTHENTICATION_FAILED}（默认）。
 * 可在构造时指定其它 {@link AuthErrorCode}（如 {@link AuthErrorCode#USER_NOT_FOUND}、
 * {@link AuthErrorCode#UNSUPPORTED_USER_TYPE}）以提供更精确的错误分类。
 */
public class AuthenticationException extends BusinessException {

    public AuthenticationException(String detail) {
        super(AuthErrorCode.AUTHENTICATION_FAILED.getCode(),
              AuthErrorCode.AUTHENTICATION_FAILED.getMessage(),
              detail);
    }

    public AuthenticationException(AuthErrorCode errorCode, String detail) {
        super(errorCode.getCode(), errorCode.getMessage(), detail);
    }

    public AuthenticationException(String detail, Throwable cause) {
        super(AuthErrorCode.AUTHENTICATION_FAILED.getCode(),
              AuthErrorCode.AUTHENTICATION_FAILED.getMessage(),
              detail,
              cause);
    }
}
