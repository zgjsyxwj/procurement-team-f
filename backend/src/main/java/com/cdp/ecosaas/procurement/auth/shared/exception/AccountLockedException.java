package com.cdp.ecosaas.procurement.auth.shared.exception;

import com.cdp.ecosaas.procurement.shared.exception.BusinessException;

/**
 * 账号锁定异常 - 账号因连续登录失败被锁定时抛出。
 * <p>
 * 错误码：{@link AuthErrorCode#ACCOUNT_LOCKED}。
 */
public class AccountLockedException extends BusinessException {

    public AccountLockedException(String detail) {
        super(AuthErrorCode.ACCOUNT_LOCKED.getCode(),
              AuthErrorCode.ACCOUNT_LOCKED.getMessage(),
              detail);
    }

    public AccountLockedException(String detail, Throwable cause) {
        super(AuthErrorCode.ACCOUNT_LOCKED.getCode(),
              AuthErrorCode.ACCOUNT_LOCKED.getMessage(),
              detail,
              cause);
    }
}
