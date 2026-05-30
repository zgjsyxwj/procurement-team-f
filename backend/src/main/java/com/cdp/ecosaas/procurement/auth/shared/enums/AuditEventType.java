package com.cdp.ecosaas.procurement.auth.shared.enums;

import lombok.Getter;

/**
 * 安全审计事件类型枚举
 */
@Getter
public enum AuditEventType {

    LOGIN_SUCCESS("登录成功"),
    LOGIN_FAILURE("登录失败"),
    LOGOUT("主动登出"),
    SESSION_TIMEOUT("会话超时"),
    PASSWORD_CHANGE("密码修改"),
    PASSWORD_RESET("密码重置"),
    ACCOUNT_LOCKED("账号锁定"),
    ACCOUNT_UNLOCKED("账号解锁"),
    ACCOUNT_CREATED("账号创建"),
    ACCOUNT_DISABLED("账号停用"),
    ACCOUNT_ENABLED("账号启用"),
    ROLE_CHANGED("角色变更");

    private final String description;

    AuditEventType(String description) {
        this.description = description;
    }
}
