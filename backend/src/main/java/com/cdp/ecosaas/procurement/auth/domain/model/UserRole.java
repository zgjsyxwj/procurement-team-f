package com.cdp.ecosaas.procurement.auth.domain.model;

import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum UserRole {

    ADMIN("采购经理"),
    BUYER("采购员"),
    BUSINESS_USER("业务人员"),
    SUPPLIER("供应商");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }
}
