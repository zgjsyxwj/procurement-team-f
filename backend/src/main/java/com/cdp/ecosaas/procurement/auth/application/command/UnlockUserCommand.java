package com.cdp.ecosaas.procurement.auth.application.command;

/**
 * 手动解锁用户命令
 *
 * @param userId 目标用户ID
 */
public record UnlockUserCommand(Long userId) {
}
