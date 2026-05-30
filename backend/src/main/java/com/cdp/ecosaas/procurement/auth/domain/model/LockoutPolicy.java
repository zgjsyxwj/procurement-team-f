package com.cdp.ecosaas.procurement.auth.domain.model;

import java.time.Duration;

/**
 * 账号锁定策略值对象。
 * <p>
 * 由 application/infrastructure 层从配置加载并显式传入聚合根，
 * 保持 domain 层不依赖 Spring。
 *
 * @param maxFailedAttempts 触发锁定的最大连续失败次数
 * @param lockDuration      锁定持续时长
 */
public record LockoutPolicy(int maxFailedAttempts, Duration lockDuration) {
    public LockoutPolicy {
        if (maxFailedAttempts < 1) {
            throw new IllegalArgumentException("maxFailedAttempts must be >= 1");
        }
        if (lockDuration == null || lockDuration.isNegative() || lockDuration.isZero()) {
            throw new IllegalArgumentException("lockDuration must be positive");
        }
    }
}
