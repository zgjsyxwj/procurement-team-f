package com.cdp.ecosaas.procurement.auth.interfaces.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 创建用户请求 DTO
 *
 * @param name  用户姓名
 * @param phone 手机号（可选，SSO用户可能为空）
 * @param email 邮箱
 * @param role  角色（ADMIN/BUYER/BUSINESS_USER）
 */
public record CreateUserRequest(
        @NotBlank(message = "姓名不能为空") String name,
        String phone,
        @NotBlank(message = "邮箱不能为空") @Email(message = "邮箱格式不正确") String email,
        @NotBlank(message = "角色不能为空") String role
) {
}
