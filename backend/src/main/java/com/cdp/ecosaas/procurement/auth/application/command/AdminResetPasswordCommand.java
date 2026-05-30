package com.cdp.ecosaas.procurement.auth.application.command;

/**
 * 管理员重置密码命令
 *
 * @param targetUserId 目标用户ID
 */
public record AdminResetPasswordCommand(Long targetUserId) {
}
