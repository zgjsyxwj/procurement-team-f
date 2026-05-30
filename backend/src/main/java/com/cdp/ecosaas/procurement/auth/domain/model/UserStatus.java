package com.cdp.ecosaas.procurement.auth.domain.model;

import lombok.Getter;

/**
 * 用户账号状态枚举
 */
@Getter
public enum UserStatus {

    ACTIVE("启用"),
    DISABLED("停用");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }
}
