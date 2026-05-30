package com.cdp.ecosaas.procurement.auth.interfaces.dto;

/**
 * 登录响应 DTO
 *
 * @param id           用户ID
 * @param name         用户姓名
 * @param role         用户角色
 * @param isFirstLogin 是否首次登录
 */
public record LoginResponse(Long id, String name, String role, boolean isFirstLogin) {
}
