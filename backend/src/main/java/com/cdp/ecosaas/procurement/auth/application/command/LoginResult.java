package com.cdp.ecosaas.procurement.auth.application.command;

/**
 * 登录结果
 *
 * @param token        JWT Token
 * @param userId       用户ID
 * @param name         用户姓名
 * @param role         用户角色
 * @param isFirstLogin 是否首次登录
 */
public record LoginResult(String token, Long userId, String name, String role, boolean isFirstLogin) {
}
