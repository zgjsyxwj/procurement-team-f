package com.cdp.ecosaas.procurement.auth.interfaces.dto;

/**
 * 创建用户响应 DTO
 *
 * @param id      新创建的用户ID
 * @param message 操作结果消息
 */
public record CreateUserResponse(Long id, String message) {
}
