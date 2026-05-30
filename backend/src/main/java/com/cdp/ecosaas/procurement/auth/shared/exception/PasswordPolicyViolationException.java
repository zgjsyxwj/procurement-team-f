package com.cdp.ecosaas.procurement.auth.shared.exception;

import com.cdp.ecosaas.procurement.shared.exception.BusinessException;

import java.util.List;

/**
 * 密码策略违规异常 - 密码不满足复杂度要求或历史限制时抛出。
 * <p>
 * 错误码：{@link AuthErrorCode#PASSWORD_POLICY_VIOLATION}（默认），
 * 历史命中场景使用 {@link AuthErrorCode#PASSWORD_HISTORY_VIOLATION}。
 */
public class PasswordPolicyViolationException extends BusinessException {

    private final List<String> violations;

    public PasswordPolicyViolationException(String detail) {
        super(AuthErrorCode.PASSWORD_POLICY_VIOLATION.getCode(),
              AuthErrorCode.PASSWORD_POLICY_VIOLATION.getMessage(),
              detail);
        this.violations = List.of(detail);
    }

    public PasswordPolicyViolationException(AuthErrorCode errorCode, String detail) {
        super(errorCode.getCode(), errorCode.getMessage(), detail);
        this.violations = List.of(detail);
    }

    public PasswordPolicyViolationException(List<String> violations) {
        super(AuthErrorCode.PASSWORD_POLICY_VIOLATION.getCode(),
              AuthErrorCode.PASSWORD_POLICY_VIOLATION.getMessage(),
              "密码不满足安全策略: " + String.join(", ", violations));
        this.violations = List.copyOf(violations);
    }

    public List<String> getViolations() {
        return violations;
    }
}
