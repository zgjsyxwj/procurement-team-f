package com.cdp.ecosaas.procurement.auth.application.command;

/**
 * 登出命令
 *
 * @param userId   用户ID
 * @param userType 用户类型：INTERNAL 或 SUPPLIER
 */
public record LogoutCommand(Long userId, String userType) {
}
