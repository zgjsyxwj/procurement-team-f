package com.cdp.ecosaas.procurement.auth.domain.event;

import java.time.LocalDateTime;

/**
 * 用户锁定领域事件
 * <p>
 * 当用户因连续登录失败达到阈值而被锁定时发布此事件。
 *
 * @param userId         被锁定用户的ID
 * @param lockedUntil    锁定截止时间
 * @param failedAttempts 累计失败次数
 * @param lockedAt       锁定发生时间
 */
public record UserLockedEvent(
        Long userId,
        LocalDateTime lockedUntil,
        int failedAttempts,
        LocalDateTime lockedAt
) {
}
