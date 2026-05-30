package com.cdp.ecosaas.procurement.auth.application.command;

/**
 * 忘记密码命令
 *
 * @param email    用户注册邮箱
 * @param userType 用户类型（INTERNAL / SUPPLIER）
 */
public record ForgotPasswordCommand(String email, String userType) {
}
