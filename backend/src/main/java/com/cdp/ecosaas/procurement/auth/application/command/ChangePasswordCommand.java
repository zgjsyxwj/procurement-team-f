package com.cdp.ecosaas.procurement.auth.application.command;

/**
 * 修改密码命令
 *
 * @param userId      用户ID
 * @param userType    用户类型（INTERNAL / SUPPLIER）
 * @param oldPassword 旧密码明文
 * @param newPassword 新密码明文
 */
public record ChangePasswordCommand(Long userId, String userType, String oldPassword, String newPassword) {
}
