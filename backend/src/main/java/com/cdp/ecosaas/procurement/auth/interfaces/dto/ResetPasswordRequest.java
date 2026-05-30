package com.cdp.ecosaas.procurement.auth.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 重置密码请求 DTO
 *
 * @param token       重置令牌
 * @param newPassword 新密码
 */
public record ResetPasswordRequest(
        @NotBlank(message = "重置令牌不能为空") String token,
        @NotBlank(message = "新密码不能为空") String newPassword
) {
}
