package com.cdp.ecosaas.procurement.auth.application.handler;

import com.cdp.ecosaas.procurement.auth.application.command.AdminResetPasswordCommand;
import com.cdp.ecosaas.procurement.auth.application.command.ChangePasswordCommand;
import com.cdp.ecosaas.procurement.auth.application.command.ForgotPasswordCommand;
import com.cdp.ecosaas.procurement.auth.application.command.ResetPasswordCommand;
import com.cdp.ecosaas.procurement.auth.application.service.AuditLogService;
import com.cdp.ecosaas.procurement.auth.domain.model.InternalUser;
import com.cdp.ecosaas.procurement.auth.domain.model.PasswordHistory;
import com.cdp.ecosaas.procurement.auth.domain.model.PasswordResetToken;
import com.cdp.ecosaas.procurement.auth.domain.model.SupplierUser;
import com.cdp.ecosaas.procurement.auth.domain.port.EmailPort;
import com.cdp.ecosaas.procurement.auth.domain.port.PasswordEncoderPort;
import com.cdp.ecosaas.procurement.auth.domain.repository.InternalUserRepository;
import com.cdp.ecosaas.procurement.auth.domain.repository.PasswordHistoryRepository;
import com.cdp.ecosaas.procurement.auth.domain.repository.PasswordResetTokenRepository;
import com.cdp.ecosaas.procurement.auth.domain.repository.SupplierUserRepository;
import com.cdp.ecosaas.procurement.auth.domain.service.PasswordDomainService;
import com.cdp.ecosaas.procurement.auth.infrastructure.config.AuthPasswordProperties;
import com.cdp.ecosaas.procurement.auth.shared.constants.AuthConstants;
import com.cdp.ecosaas.procurement.auth.shared.enums.AuditEventType;
import com.cdp.ecosaas.procurement.auth.shared.exception.AuthErrorCode;
import com.cdp.ecosaas.procurement.auth.shared.exception.AuthenticationException;
import com.cdp.ecosaas.procurement.auth.shared.exception.PasswordPolicyViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 密码命令处理器
 * <p>
 * 处理修改密码、忘记密码、重置密码和管理员重置密码命令。
 * 协调领域服务和基础设施组件完成密码管理流程。
 * <p>
 * 重置链接 base URL、Token 有效期等配置由 {@link AuthPasswordProperties} 提供。
 */
@Service
@RequiredArgsConstructor
public class PasswordCommandHandler {

    private static final String RESET_LINK_TOKEN_PARAM = "?token=";

    private final InternalUserRepository internalUserRepository;
    private final SupplierUserRepository supplierUserRepository;
    private final PasswordDomainService passwordDomainService;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AuditLogService auditLogService;
    private final EmailPort emailPort;
    private final PasswordEncoderPort passwordEncoder;
    private final AuthPasswordProperties passwordProps;

    @Transactional
    public void handleChangePassword(ChangePasswordCommand cmd, String ipAddress) {
        // 1. 校验新密码复杂度
        List<String> violations = passwordDomainService.validateComplexity(cmd.newPassword());
        if (!violations.isEmpty()) {
            throw new PasswordPolicyViolationException(violations);
        }

        // 2. 获取密码历史并检查
        List<PasswordHistory> recentHistory = passwordHistoryRepository.findRecentByUser(cmd.userId(), cmd.userType());
        if (passwordDomainService.checkPasswordHistory(cmd.newPassword(), recentHistory, passwordEncoder)) {
            throw new PasswordPolicyViolationException(
                    AuthErrorCode.PASSWORD_HISTORY_VIOLATION,
                    "新密码不能与最近 " + passwordProps.historyCount() + " 次使用过的密码相同"
            );
        }

        // 3. 根据用户类型处理
        String userName;
        String userEmail;
        if (AuthConstants.USER_TYPE_INTERNAL.equals(cmd.userType())) {
            InternalUser user = internalUserRepository.findById(cmd.userId())
                    .orElseThrow(() -> new AuthenticationException(AuthErrorCode.USER_NOT_FOUND, "用户不存在"));
            user.changePassword(cmd.oldPassword(), cmd.newPassword(), passwordEncoder);
            internalUserRepository.save(user);
            userName = user.getName();
            userEmail = user.getEmail();
        } else if (AuthConstants.USER_TYPE_SUPPLIER.equals(cmd.userType())) {
            SupplierUser user = supplierUserRepository.findById(cmd.userId())
                    .orElseThrow(() -> new AuthenticationException(AuthErrorCode.USER_NOT_FOUND, "用户不存在"));
            user.changePassword(cmd.oldPassword(), cmd.newPassword(), passwordEncoder);
            supplierUserRepository.save(user);
            userName = user.getName();
            userEmail = user.getEmail();
        } else {
            throw new AuthenticationException(AuthErrorCode.UNSUPPORTED_USER_TYPE,
                    "不支持的用户类型: " + cmd.userType());
        }

        // 4. 保存密码历史
        savePasswordHistory(cmd.userId(), cmd.userType(), passwordEncoder.encode(cmd.newPassword()));

        // 5. 记录审计日志
        auditLogService.recordSuccess(
                AuditEventType.PASSWORD_CHANGE,
                cmd.userId(), userName,
                cmd.userId(), userName,
                ipAddress,
                "用户自行修改密码"
        );

        // 6. 发送密码变更通知
        emailPort.sendPasswordChangedNotification(userEmail, userName);
    }

    @Transactional
    public void handleForgotPassword(ForgotPasswordCommand cmd) {
        Long userId = null;
        String userName = null;
        String userEmail = null;

        if (AuthConstants.USER_TYPE_INTERNAL.equals(cmd.userType())) {
            var userOpt = internalUserRepository.findByEmail(cmd.email());
            if (userOpt.isPresent()) {
                InternalUser user = userOpt.get();
                userId = user.getId();
                userName = user.getName();
                userEmail = user.getEmail();
            }
        } else if (AuthConstants.USER_TYPE_SUPPLIER.equals(cmd.userType())) {
            var userOpt = supplierUserRepository.findByEmail(cmd.email());
            if (userOpt.isPresent()) {
                SupplierUser user = userOpt.get();
                userId = user.getId();
                userName = user.getName();
                userEmail = user.getEmail();
            }
        }

        if (userId != null) {
            String token = UUID.randomUUID().toString();
            LocalDateTime expiresAt = LocalDateTime.now().plus(passwordProps.resetTokenExpiry());

            PasswordResetToken resetToken = new PasswordResetToken(
                    userId, cmd.userType(), token, expiresAt, false, LocalDateTime.now()
            );
            passwordResetTokenRepository.save(resetToken);

            String resetLink = passwordProps.resetBaseUrl() + RESET_LINK_TOKEN_PARAM + token;
            emailPort.sendPasswordResetLink(userEmail, userName, resetLink);
        }
        // 无论用户是否存在，都不透露信息
    }

    @Transactional
    public void handleResetPassword(ResetPasswordCommand cmd, String ipAddress) {
        // 1. 查找并验证Token
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(cmd.token())
                .orElseThrow(() -> new AuthenticationException(AuthErrorCode.RESET_TOKEN_INVALID, "重置链接无效"));

        if (resetToken.used()) {
            throw new AuthenticationException(AuthErrorCode.RESET_TOKEN_USED, "重置链接已被使用");
        }

        if (resetToken.isExpired()) {
            throw new AuthenticationException(AuthErrorCode.RESET_TOKEN_EXPIRED, "重置链接已过期，请重新申请");
        }

        // 2. 校验新密码复杂度
        List<String> violations = passwordDomainService.validateComplexity(cmd.newPassword());
        if (!violations.isEmpty()) {
            throw new PasswordPolicyViolationException(violations);
        }

        // 3. 检查密码历史
        List<PasswordHistory> recentHistory = passwordHistoryRepository.findRecentByUser(
                resetToken.userId(), resetToken.userType());
        if (passwordDomainService.checkPasswordHistory(cmd.newPassword(), recentHistory, passwordEncoder)) {
            throw new PasswordPolicyViolationException(
                    AuthErrorCode.PASSWORD_HISTORY_VIOLATION,
                    "新密码不能与最近 " + passwordProps.historyCount() + " 次使用过的密码相同"
            );
        }

        // 4. 更新用户密码
        String newPasswordHash = passwordDomainService.hashPassword(cmd.newPassword(), passwordEncoder);
        String userName;
        String userEmail;

        if (AuthConstants.USER_TYPE_INTERNAL.equals(resetToken.userType())) {
            InternalUser user = internalUserRepository.findById(resetToken.userId())
                    .orElseThrow(() -> new AuthenticationException(AuthErrorCode.USER_NOT_FOUND, "用户不存在"));
            user.resetPassword(newPasswordHash, false);
            internalUserRepository.save(user);
            userName = user.getName();
            userEmail = user.getEmail();
        } else {
            SupplierUser user = supplierUserRepository.findById(resetToken.userId())
                    .orElseThrow(() -> new AuthenticationException(AuthErrorCode.USER_NOT_FOUND, "用户不存在"));
            user.resetPassword(newPasswordHash, false);
            supplierUserRepository.save(user);
            userName = user.getName();
            userEmail = user.getEmail();
        }

        // 5. 标记Token已使用
        passwordResetTokenRepository.markAsUsed(cmd.token());

        // 6. 保存密码历史
        savePasswordHistory(resetToken.userId(), resetToken.userType(), newPasswordHash);

        // 7. 记录审计日志
        auditLogService.recordSuccess(
                AuditEventType.PASSWORD_RESET,
                resetToken.userId(), userName,
                resetToken.userId(), userName,
                ipAddress,
                "用户通过重置链接重置密码"
        );

        // 8. 发送密码变更通知
        emailPort.sendPasswordChangedNotification(userEmail, userName);
    }

    @Transactional
    public void handleAdminResetPassword(AdminResetPasswordCommand cmd, Long operatorId, String ipAddress) {
        InternalUser targetUser = internalUserRepository.findById(cmd.targetUserId())
                .orElseThrow(() -> new AuthenticationException(AuthErrorCode.USER_NOT_FOUND, "目标用户不存在"));

        String randomPassword = passwordDomainService.generateRandomPassword();

        String newPasswordHash = passwordDomainService.hashPassword(randomPassword, passwordEncoder);
        targetUser.resetPassword(newPasswordHash, true);
        internalUserRepository.save(targetUser);

        savePasswordHistory(targetUser.getId(), AuthConstants.USER_TYPE_INTERNAL, newPasswordHash);

        emailPort.sendInitialPassword(targetUser.getEmail(), targetUser.getName(), randomPassword);

        auditLogService.recordSuccess(
                AuditEventType.PASSWORD_RESET,
                operatorId, null,
                targetUser.getId(), targetUser.getName(),
                ipAddress,
                "管理员重置用户密码"
        );
    }

    private void savePasswordHistory(Long userId, String userType, String passwordHash) {
        PasswordHistory history = PasswordHistory.builder()
                .userId(userId)
                .userType(userType)
                .passwordHash(passwordHash)
                .createdAt(LocalDateTime.now())
                .build();
        passwordHistoryRepository.save(history);
    }
}
