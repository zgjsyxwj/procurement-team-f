package com.cdp.ecosaas.procurement.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 密码历史值对象
 * <p>
 * 记录用户的历史密码哈希，用于密码策略中"禁止与最近5次密码相同"的校验。
 * 这是一个不可变值对象，不包含 JPA 注解。
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PasswordHistory {

    /** 用户ID */
    private Long userId;

    /** 用户类型：INTERNAL / SUPPLIER */
    private String userType;

    /** 历史密码哈希 */
    private String passwordHash;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
