package com.cdp.ecosaas.procurement.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 登录尝试值对象
 * <p>
 * 记录一次登录尝试的详细信息，用于审计和锁定逻辑。
 * 这是一个不可变值对象，不包含 JPA 注解。
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginAttempt {

    /** 用户ID */
    private Long userId;

    /** 用户类型：INTERNAL / SUPPLIER */
    private String userType;

    /** 登录是否成功 */
    private boolean success;

    /** 登录来源 IP 地址 */
    private String ipAddress;

    /** 尝试时间 */
    private LocalDateTime attemptTime;
}
