package com.cdp.ecosaas.procurement.auth.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 修改用户状态请求 DTO
 *
 * @param status 新状态（ACTIVE/DISABLED）
 */
public record UpdateStatusRequest(
        @NotBlank(message = "状态不能为空") String status
) {
}
