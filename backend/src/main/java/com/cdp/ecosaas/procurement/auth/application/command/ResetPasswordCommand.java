package com.cdp.ecosaas.procurement.auth.application.command;

/**
 * 重置密码命令（通过重置链接）
 *
 * @param token       密码重置令牌
 * @param newPassword 新密码明文
 */
public record ResetPasswordCommand(String token, String newPassword) {
}
