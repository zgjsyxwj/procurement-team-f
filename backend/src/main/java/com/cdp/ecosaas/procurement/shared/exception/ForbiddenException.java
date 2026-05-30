package com.cdp.ecosaas.procurement.shared.exception;

/**
 * 权限不足异常。
 * <p>
 * 当用户尝试执行超出其角色权限的操作时抛出。
 * <p>
 * 错误码：{@code COMMON.1003} / {@code FORBIDDEN}。
 */
public class ForbiddenException extends BusinessException {

    public static final String CODE = "COMMON.1003";
    public static final String MESSAGE_CODE = "FORBIDDEN";

    public ForbiddenException(String detail) {
        super(CODE, MESSAGE_CODE, detail);
    }

    public ForbiddenException() {
        super(CODE, MESSAGE_CODE, "权限不足");
    }
}
