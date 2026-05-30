package com.cdp.ecosaas.procurement.auth.application.command;

/**
 * 创建内部用户命令
 *
 * @param name  用户姓名
 * @param phone 手机号（可选，SSO用户可能为空）
 * @param email 邮箱
 * @param role  角色（ADMIN/BUYER/BUSINESS_USER）
 */
public record CreateInternalUserCommand(String name, String phone, String email, String role) {
}
