package com.cdp.ecosaas.procurement.auth.application.command;

/**
 * 登录命令
 *
 * @param phone    手机号
 * @param password 密码
 * @param userType 用户类型：INTERNAL 或 SUPPLIER
 */
public record LoginCommand(String phone, String password, String userType) {
}
