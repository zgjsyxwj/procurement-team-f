package com.cdp.ecosaas.procurement.auth.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 修改用户角色请求 DTO
 *
 * @param role 新角色
 */
public record UpdateRoleRequest(
        @NotBlank(message = "角色不能为空") String role
) {
}
