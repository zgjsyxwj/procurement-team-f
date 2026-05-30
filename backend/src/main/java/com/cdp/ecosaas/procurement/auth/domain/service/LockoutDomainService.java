package com.cdp.ecosaas.procurement.auth.domain.service;

import com.cdp.ecosaas.procurement.auth.domain.model.InternalUser;
import com.cdp.ecosaas.procurement.auth.domain.model.LockoutPolicy;
import com.cdp.ecosaas.procurement.auth.domain.model.SupplierUser;

import java.time.LocalDateTime;

/**
 * 锁定领域服务
 * <p>
 * 提供账号锁定相关的业务逻辑协调层，包括：
 * <ul>
 *   <li>登录失败计数递增与锁定判断</li>
 *   <li>自动解锁判断（锁定时间到期后自动解锁并重置计数器）</li>
 *   <li>手动解锁逻辑</li>
 * </ul>
 * <p>
 * 实际的 lock/unlock 操作委托给聚合根执行；锁定策略 {@link LockoutPolicy}
 * 由基础设施层从 {@code AuthLockoutProperties} 注入。
 * 不依赖 Spring 框架（参见 backend_spec §3.1）。
 */
public class LockoutDomainService {

    private final LockoutPolicy policy;

    public LockoutDomainService(LockoutPolicy policy) {
        this.policy = policy;
    }

    /**
     * 当前生效的锁定策略。
     */
    public LockoutPolicy policy() {
        return policy;
    }

    /**
     * 判断是否应该锁定账号。
     */
    public boolean shouldLock(int failedAttempts) {
        return failedAttempts >= policy.maxFailedAttempts();
    }

    /**
     * 判断是否满足自动解锁条件。
     */
    public boolean isAutoUnlockEligible(LocalDateTime lockedUntil) {
        if (lockedUntil == null) {
            return false;
        }
        return !lockedUntil.isAfter(LocalDateTime.now());
    }

    /**
     * 记录内部用户登录失败：递增计数，达到阈值则锁定。
     */
    public void recordFailedAttempt(InternalUser user) {
        user.incrementFailedAttempts();
        if (shouldLock(user.getFailedAttempts())) {
            user.lock(policy);
        }
    }

    /**
     * 记录供应商用户登录失败：递增计数，达到阈值则锁定。
     */
    public void recordFailedAttempt(SupplierUser user) {
        user.incrementFailedAttempts();
        if (shouldLock(user.getFailedAttempts())) {
            user.lock(policy);
        }
    }

    /**
     * 手动解锁内部用户。
     */
    public void manualUnlock(InternalUser user) {
        user.unlock();
    }

    /**
     * 手动解锁供应商用户。
     */
    public void manualUnlock(SupplierUser user) {
        user.unlock();
    }

    /**
     * 检查并自动解锁内部用户。
     */
    public boolean checkAndAutoUnlock(InternalUser user) {
        if (isAutoUnlockEligible(user.getLockedUntil())) {
            user.unlock();
            return true;
        }
        return false;
    }

    /**
     * 检查并自动解锁供应商用户。
     */
    public boolean checkAndAutoUnlock(SupplierUser user) {
        if (isAutoUnlockEligible(user.getLockedUntil())) {
            user.unlock();
            return true;
        }
        return false;
    }
}
