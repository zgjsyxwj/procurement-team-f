package com.cdp.ecosaas.procurement.auth.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求 DTO
 *
 * @param phone    手机号
 * @param password 密码
 */
public record LoginRequest(
        @NotBlank(message = "手机号不能为空") String phone,
        @NotBlank(message = "密码不能为空") String password
) {
}
