package com.cdp.ecosaas.procurement.auth.domain.event;

import java.time.LocalDateTime;

/**
 * 密码变更领域事件
 * <p>
 * 当用户密码被修改（自行修改或管理员重置）时发布此事件。
 *
 * @param userId    用户ID
 * @param userType  用户类型：INTERNAL / SUPPLIER
 * @param changedAt 密码变更时间
 */
public record PasswordChangedEvent(
        Long userId,
        String userType,
        LocalDateTime changedAt
) {
}
