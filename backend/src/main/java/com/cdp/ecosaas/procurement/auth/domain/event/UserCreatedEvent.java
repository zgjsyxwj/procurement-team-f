package com.cdp.ecosaas.procurement.auth.domain.event;

import java.time.LocalDateTime;

/**
 * 用户创建领域事件
 * <p>
 * 当新用户（内部用户或供应商）被创建时发布此事件。
 *
 * @param userId    新创建用户的ID
 * @param name      用户姓名
 * @param role      用户角色
 * @param createdAt 创建时间
 */
public record UserCreatedEvent(
        Long userId,
        String name,
        String role,
        LocalDateTime createdAt
) {
}
