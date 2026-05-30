package com.cdp.ecosaas.procurement.auth.domain.model;

import com.cdp.ecosaas.procurement.auth.domain.port.PasswordEncoderPort;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 内部用户聚合根
 * <p>
 * 包含认证、锁定、密码管理、角色变更等核心业务逻辑。
 * 这是一个纯领域模型，不包含 JPA 注解或 Spring 依赖。
 * <p>
 * 锁定阈值与时长通过 {@link LockoutPolicy} 由调用方显式传入，
 * 保持领域层无外部配置依赖（参见 backend_spec §3.1、§12.3）。
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InternalUser {

    private Long id;
    private String name;
    private String phone;
    private String email;
    private String passwordHash;
    private UserRole role;
    private UserStatus status;
    private String ssoSubjectId;
    private boolean isSuperAdmin;
    private boolean isFirstLogin;
    private int failedAttempts;
    private LocalDateTime lockedUntil;

    /**
     * 验证用户密码。
     * <p>
     * 流程：检查是否锁定 → 验证密码 → 失败则递增计数 → 达到阈值则锁定。
     *
     * @param rawPassword     用户输入的明文密码
     * @param passwordEncoder 密码编码器端口
     * @param policy          锁定策略（阈值与时长）
     * @return true 如果密码验证通过
     */
    public boolean authenticate(String rawPassword, PasswordEncoderPort passwordEncoder, LockoutPolicy policy) {
        if (isLocked()) {
            return false;
        }

        if (passwordEncoder.matches(rawPassword, this.passwordHash)) {
            // 验证成功，重置失败计数
            this.failedAttempts = 0;
            this.isFirstLogin = false;
            return true;
        }

        // 验证失败，递增失败计数
        this.failedAttempts++;
        if (this.failedAttempts >= policy.maxFailedAttempts()) {
            lock(policy);
        }
        return false;
    }

    /**
     * 递增登录失败计数。
     */
    public void incrementFailedAttempts() {
        this.failedAttempts++;
    }

    /**
     * 锁定账号 - 锁定时长由策略决定。
     */
    public void lock(LockoutPolicy policy) {
        this.lockedUntil = LocalDateTime.now().plus(policy.lockDuration());
    }

    /**
     * 手动解锁账号，重置失败计数和锁定时间。
     */
    public void unlock() {
        this.failedAttempts = 0;
        this.lockedUntil = null;
    }

    /**
     * 判断账号是否处于锁定状态。
     * <p>
     * 如果 lockedUntil 不为空且在当前时间之后，则账号处于锁定状态。
     *
     * @return true 如果账号当前被锁定
     */
    public boolean isLocked() {
        return this.lockedUntil != null && this.lockedUntil.isAfter(LocalDateTime.now());
    }

    /**
     * 修改密码。
     * <p>
     * 验证旧密码后，将新密码哈希存储。
     *
     * @param oldPassword     旧密码明文
     * @param newPassword     新密码明文
     * @param passwordEncoder 密码编码器端口
     * @throws IllegalArgumentException 如果旧密码验证失败
     */
    public void changePassword(String oldPassword, String newPassword, PasswordEncoderPort passwordEncoder) {
        if (!passwordEncoder.matches(oldPassword, this.passwordHash)) {
            throw new IllegalArgumentException("旧密码验证失败");
        }
        this.passwordHash = passwordEncoder.encode(newPassword);
        this.isFirstLogin = false;
    }

    /**
     * 重置密码（无需验证旧密码）。
     * <p>
     * 用于管理员重置密码或通过重置链接设置新密码的场景。
     *
     * @param newPasswordHash 新密码的哈希值
     * @param markFirstLogin  是否标记为首次登录（管理员重置时为 true）
     */
    public void resetPassword(String newPasswordHash, boolean markFirstLogin) {
        this.passwordHash = newPasswordHash;
        this.isFirstLogin = markFirstLogin;
    }

    /**
     * 修改用户角色。
     * <p>
     * 超级管理员不可被降级（角色不可变更）。
     *
     * @param newRole 新角色
     * @throws IllegalStateException 如果用户是超级管理员
     */
    public void changeRole(UserRole newRole) {
        if (this.isSuperAdmin) {
            throw new IllegalStateException("超级管理员角色不可变更");
        }
        this.role = newRole;
    }

    /**
     * 停用账号。
     * <p>
     * 超级管理员不可被停用。
     *
     * @throws IllegalStateException 如果用户是超级管理员
     */
    public void disable() {
        if (this.isSuperAdmin) {
            throw new IllegalStateException("超级管理员不可被停用");
        }
        this.status = UserStatus.DISABLED;
    }

    /**
     * 启用账号。
     */
    public void enable() {
        this.status = UserStatus.ACTIVE;
    }
}
