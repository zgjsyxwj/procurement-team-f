package com.cdp.ecosaas.procurement.auth.application.handler;

import com.cdp.ecosaas.procurement.auth.application.command.CreateInternalUserCommand;
import com.cdp.ecosaas.procurement.auth.application.command.UnlockUserCommand;
import com.cdp.ecosaas.procurement.auth.application.command.UpdateUserRoleCommand;
import com.cdp.ecosaas.procurement.auth.application.command.UpdateUserStatusCommand;
import com.cdp.ecosaas.procurement.auth.application.service.AuditLogService;
import com.cdp.ecosaas.procurement.auth.domain.model.InternalUser;
import com.cdp.ecosaas.procurement.auth.domain.model.PasswordHistory;
import com.cdp.ecosaas.procurement.auth.domain.model.UserRole;
import com.cdp.ecosaas.procurement.auth.domain.model.UserStatus;
import com.cdp.ecosaas.procurement.auth.domain.port.EmailPort;
import com.cdp.ecosaas.procurement.auth.domain.port.PasswordEncoderPort;
import com.cdp.ecosaas.procurement.auth.domain.repository.InternalUserRepository;
import com.cdp.ecosaas.procurement.auth.domain.repository.PasswordHistoryRepository;
import com.cdp.ecosaas.procurement.auth.domain.service.PasswordDomainService;
import com.cdp.ecosaas.procurement.auth.shared.constants.AuthConstants;
import com.cdp.ecosaas.procurement.auth.shared.enums.AuditEventType;
import com.cdp.ecosaas.procurement.auth.shared.exception.AuthErrorCode;
import com.cdp.ecosaas.procurement.auth.shared.exception.AuthenticationException;
import com.cdp.ecosaas.procurement.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户管理命令处理器
 * <p>
 * 处理创建内部用户、修改角色、停用/启用、手动解锁等管理操作。
 * 协调领域模型、领域服务和基础设施组件完成用户管理流程。
 */
@Service
@RequiredArgsConstructor
public class UserCommandHandler {

    private final InternalUserRepository internalUserRepository;
    private final PasswordDomainService passwordDomainService;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final AuditLogService auditLogService;
    private final EmailPort emailPort;
    private final PasswordEncoderPort passwordEncoder;

    /**
     * 处理创建内部用户命令。
     * <p>
     * 流程：校验手机号唯一 → 校验邮箱唯一 → 生成初始密码 → 创建用户 → 保存 → 保存密码历史 → 发送邮件 → 审计日志
     *
     * @param cmd        创建用户命令
     * @param operatorId 操作人（管理员）ID
     * @param ipAddress  客户端IP地址
     * @return 创建的用户ID
     * @throws IllegalArgumentException 手机号或邮箱已存在
     */
    @Transactional
    public Long handleCreateUser(CreateInternalUserCommand cmd, Long operatorId, String ipAddress) {
        // 1. 校验手机号唯一（如果提供了手机号）
        if (cmd.phone() != null && !cmd.phone().isBlank()) {
            internalUserRepository.findByPhone(cmd.phone()).ifPresent(existing -> {
                throw new BusinessException(
                        AuthErrorCode.PHONE_ALREADY_USED.getCode(),
                        AuthErrorCode.PHONE_ALREADY_USED.getMessage(),
                        "手机号已被使用");
            });
        }

        // 2. 校验邮箱唯一
        internalUserRepository.findByEmail(cmd.email()).ifPresent(existing -> {
            throw new BusinessException(
                    AuthErrorCode.EMAIL_ALREADY_USED.getCode(),
                    AuthErrorCode.EMAIL_ALREADY_USED.getMessage(),
                    "邮箱已被使用");
        });

        // 3. 生成随机初始密码
        String randomPassword = passwordDomainService.generateRandomPassword();
        String passwordHash = passwordDomainService.hashPassword(randomPassword, passwordEncoder);

        // 4. 创建 InternalUser 领域对象
        InternalUser newUser = InternalUser.builder()
                .name(cmd.name())
                .phone(cmd.phone())
                .email(cmd.email())
                .passwordHash(passwordHash)
                .role(UserRole.valueOf(cmd.role()))
                .status(UserStatus.ACTIVE)
                .isSuperAdmin(false)
                .isFirstLogin(true)
                .failedAttempts(0)
                .build();

        // 5. 保存用户
        InternalUser savedUser = internalUserRepository.save(newUser);

        // 6. 保存密码历史
        savePasswordHistory(savedUser.getId(), passwordHash);

        // 7. 发送初始密码邮件
        emailPort.sendInitialPassword(cmd.email(), cmd.name(), randomPassword);

        // 8. 记录审计日志
        auditLogService.recordSuccess(
                AuditEventType.ACCOUNT_CREATED,
                operatorId, null,
                savedUser.getId(), savedUser.getName(),
                ipAddress,
                "创建内部用户，角色: " + cmd.role()
        );

        return savedUser.getId();
    }

    /**
     * 处理修改用户角色命令。
     */
    @Transactional
    public void handleUpdateRole(UpdateUserRoleCommand cmd, Long operatorId, String ipAddress) {
        InternalUser user = internalUserRepository.findById(cmd.userId())
                .orElseThrow(() -> new AuthenticationException(AuthErrorCode.USER_NOT_FOUND, "用户不存在"));

        String oldRole = user.getRole().name();
        user.changeRole(UserRole.valueOf(cmd.newRole()));
        internalUserRepository.save(user);

        auditLogService.recordSuccess(
                AuditEventType.ROLE_CHANGED,
                operatorId, null,
                user.getId(), user.getName(),
                ipAddress,
                "角色变更: " + oldRole + " → " + cmd.newRole()
        );
    }

    /**
     * 处理修改用户状态命令（停用/启用）。
     */
    @Transactional
    public void handleUpdateStatus(UpdateUserStatusCommand cmd, Long operatorId, String ipAddress) {
        InternalUser user = internalUserRepository.findById(cmd.userId())
                .orElseThrow(() -> new AuthenticationException(AuthErrorCode.USER_NOT_FOUND, "用户不存在"));

        UserStatus targetStatus = UserStatus.valueOf(cmd.newStatus());
        AuditEventType eventType;

        if (UserStatus.DISABLED == targetStatus) {
            user.disable();
            eventType = AuditEventType.ACCOUNT_DISABLED;
        } else {
            user.enable();
            eventType = AuditEventType.ACCOUNT_ENABLED;
        }

        internalUserRepository.save(user);

        auditLogService.recordSuccess(
                eventType,
                operatorId, null,
                user.getId(), user.getName(),
                ipAddress,
                "账号状态变更为: " + targetStatus.getDescription()
        );
    }

    /**
     * 处理手动解锁用户命令。
     */
    @Transactional
    public void handleUnlock(UnlockUserCommand cmd, Long operatorId, String ipAddress) {
        InternalUser user = internalUserRepository.findById(cmd.userId())
                .orElseThrow(() -> new AuthenticationException(AuthErrorCode.USER_NOT_FOUND, "用户不存在"));

        user.unlock();
        internalUserRepository.save(user);

        auditLogService.recordSuccess(
                AuditEventType.ACCOUNT_UNLOCKED,
                operatorId, null,
                user.getId(), user.getName(),
                ipAddress,
                "管理员手动解锁账号"
        );
    }

    // ==================== 私有方法 ====================

    private void savePasswordHistory(Long userId, String passwordHash) {
        PasswordHistory history = PasswordHistory.builder()
                .userId(userId)
                .userType(AuthConstants.USER_TYPE_INTERNAL)
                .passwordHash(passwordHash)
                .createdAt(LocalDateTime.now())
                .build();
        passwordHistoryRepository.save(history);
    }
}
