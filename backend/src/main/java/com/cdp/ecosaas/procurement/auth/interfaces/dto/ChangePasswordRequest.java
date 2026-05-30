package com.cdp.ecosaas.procurement.auth.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 修改密码请求 DTO
 *
 * @param oldPassword 旧密码
 * @param newPassword 新密码
 */
public record ChangePasswordRequest(
        @NotBlank(message = "旧密码不能为空") String oldPassword,
        @NotBlank(message = "新密码不能为空") String newPassword
) {
}
