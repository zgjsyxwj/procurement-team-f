package com.cdp.ecosaas.procurement.auth.application.handler;

import com.cdp.ecosaas.procurement.auth.application.command.LoginCommand;
import com.cdp.ecosaas.procurement.auth.application.command.LoginResult;
import com.cdp.ecosaas.procurement.auth.application.command.LogoutCommand;
import com.cdp.ecosaas.procurement.auth.application.command.SsoLoginCommand;
import com.cdp.ecosaas.procurement.auth.application.service.AuditLogService;
import com.cdp.ecosaas.procurement.auth.domain.model.InternalUser;
import com.cdp.ecosaas.procurement.auth.domain.model.SupplierUser;
import com.cdp.ecosaas.procurement.auth.domain.model.UserRole;
import com.cdp.ecosaas.procurement.auth.domain.model.UserStatus;
import com.cdp.ecosaas.procurement.auth.domain.port.PasswordEncoderPort;
import com.cdp.ecosaas.procurement.auth.domain.port.SamlPort;
import com.cdp.ecosaas.procurement.auth.domain.port.SamlUserAttributes;
import com.cdp.ecosaas.procurement.auth.domain.port.TokenPort;
import com.cdp.ecosaas.procurement.auth.domain.repository.InternalUserRepository;
import com.cdp.ecosaas.procurement.auth.domain.repository.SupplierUserRepository;
import com.cdp.ecosaas.procurement.auth.domain.service.LockoutDomainService;
import com.cdp.ecosaas.procurement.auth.shared.constants.AuthConstants;
import com.cdp.ecosaas.procurement.auth.shared.enums.AuditEventType;
import com.cdp.ecosaas.procurement.auth.shared.exception.AccountLockedException;
import com.cdp.ecosaas.procurement.auth.shared.exception.AuthErrorCode;
import com.cdp.ecosaas.procurement.auth.shared.exception.AuthenticationException;
import com.cdp.ecosaas.procurement.shared.event.SupplierFirstLoginEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 认证命令处理器
 * <p>
 * 处理登录、SSO登录和登出命令，协调领域服务和基础设施组件完成认证流程。
 */
@Service
@RequiredArgsConstructor
public class AuthCommandHandler {

    private final InternalUserRepository internalUserRepository;
    private final SupplierUserRepository supplierUserRepository;
    private final TokenPort tokenPort;
    private final SamlPort samlPort;
    private final LockoutDomainService lockoutDomainService;
    private final AuditLogService auditLogService;
    private final PasswordEncoderPort passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 处理手机号密码登录。
     * <p>
     * 流程：查找用户 → 检查锁定 → 检查状态 → 验证密码 → 生成Token → 记录审计日志
     *
     * @param cmd       登录命令
     * @param ipAddress 客户端IP地址
     * @return 登录结果
     * @throws AuthenticationException 凭据验证失败
     * @throws AccountLockedException  账号被锁定
     */
    @Transactional
    public LoginResult handleLogin(LoginCommand cmd, String ipAddress) {
        if (AuthConstants.USER_TYPE_INTERNAL.equals(cmd.userType())) {
            return handleInternalLogin(cmd, ipAddress);
        } else if (AuthConstants.USER_TYPE_SUPPLIER.equals(cmd.userType())) {
            return handleSupplierLogin(cmd, ipAddress);
        }
        throw new AuthenticationException(AuthErrorCode.UNSUPPORTED_USER_TYPE, "不支持的用户类型: " + cmd.userType());
    }

    /**
     * 处理 SSO 登录（SAML 2.0）。
     * <p>
     * 流程：解析SAML → 提取用户属性 → 查找/创建用户（JIT Provisioning） → 生成Token → 记录审计日志
     *
     * @param cmd       SSO登录命令
     * @param ipAddress 客户端IP地址
     * @return 登录结果
     */
    @Transactional
    public LoginResult handleSsoLogin(SsoLoginCommand cmd, String ipAddress) {
        // 1. 解析 SAML Response
        Map<String, String> samlAttributes = samlPort.parseSamlResponse(cmd.samlResponse());

        // 2. 提取用户属性
        SamlUserAttributes userAttributes = samlPort.extractUserAttributes(samlAttributes);

        // 3. 查找或创建用户（JIT Provisioning）
        InternalUser user = internalUserRepository.findBySsoSubjectId(userAttributes.subjectId())
                .orElseGet(() -> createSsoUser(userAttributes));

        // 4. 生成 Token
        String token = tokenPort.generateToken(
                user.getId(),
                AuthConstants.USER_TYPE_INTERNAL,
                user.getRole().name(),
                user.getName()
        );

        // 5. 记录审计日志
        auditLogService.recordSuccess(
                AuditEventType.LOGIN_SUCCESS,
                user.getId(),
                user.getName(),
                user.getId(),
                user.getName(),
                ipAddress,
                "SSO登录成功"
        );

        return new LoginResult(token, user.getId(), user.getName(), user.getRole().name(), user.isFirstLogin());
    }

    /**
     * 处理登出。
     * <p>
     * 流程：记录审计日志 → 使Token失效
     *
     * @param cmd       登出命令
     * @param ipAddress 客户端IP地址
     */
    @Transactional
    public void handleLogout(LogoutCommand cmd, String ipAddress) {
        // 1. 记录审计日志
        auditLogService.recordSuccess(
                AuditEventType.LOGOUT,
                cmd.userId(),
                null,
                cmd.userId(),
                null,
                ipAddress,
                "用户主动登出"
        );

        // 2. 使 Token 失效（当前为无状态JWT，通过清除Cookie实现）
        tokenPort.invalidateToken(null);
    }

    // ==================== 私有方法 ====================

    private LoginResult handleInternalLogin(LoginCommand cmd, String ipAddress) {
        // 1. 查找用户
        InternalUser user = internalUserRepository.findByPhone(cmd.phone())
                .orElseThrow(() -> {
                    auditLogService.recordFailure(
                            AuditEventType.LOGIN_FAILURE,
                            null, null, null, null,
                            ipAddress,
                            "手机号不存在: " + cmd.phone()
                    );
                    return new AuthenticationException("手机号或密码错误");
                });

        // 2. 检查锁定状态
        if (user.isLocked()) {
            // 检查是否满足自动解锁条件
            if (!lockoutDomainService.checkAndAutoUnlock(user)) {
                auditLogService.recordFailure(
                        AuditEventType.LOGIN_FAILURE,
                        null, null, user.getId(), user.getName(),
                        ipAddress,
                        "账号已锁定"
                );
                throw new AccountLockedException("账号已锁定，请30分钟后重试或联系管理员");
            }
            // 自动解锁成功，保存状态
            internalUserRepository.save(user);
        }

        // 3. 检查账号状态
        if (UserStatus.DISABLED == user.getStatus()) {
            auditLogService.recordFailure(
                    AuditEventType.LOGIN_FAILURE,
                    null, null, user.getId(), user.getName(),
                    ipAddress,
                    "账号已停用"
            );
            throw new AuthenticationException(AuthErrorCode.ACCOUNT_DISABLED, "账号已停用，请联系管理员");
        }

        // 4. 验证密码
        boolean authenticated = user.authenticate(cmd.password(), passwordEncoder, lockoutDomainService.policy());
        if (!authenticated) {
            // 保存用户（持久化 failedAttempts 变更）
            internalUserRepository.save(user);

            // 检查是否因此次失败被锁定
            if (user.isLocked()) {
                auditLogService.recordSuccess(
                        AuditEventType.ACCOUNT_LOCKED,
                        null, null, user.getId(), user.getName(),
                        ipAddress,
                        "连续登录失败达到阈值，账号已锁定"
                );
            }

            auditLogService.recordFailure(
                    AuditEventType.LOGIN_FAILURE,
                    null, null, user.getId(), user.getName(),
                    ipAddress,
                    "密码验证失败"
            );
            throw new AuthenticationException("手机号或密码错误");
        }

        // 5. 登录成功：保存用户（重置 failedAttempts）
        internalUserRepository.save(user);

        // 6. 生成 Token
        String token = tokenPort.generateToken(
                user.getId(),
                AuthConstants.USER_TYPE_INTERNAL,
                user.getRole().name(),
                user.getName()
        );

        // 7. 记录审计日志
        auditLogService.recordSuccess(
                AuditEventType.LOGIN_SUCCESS,
                user.getId(), user.getName(), user.getId(), user.getName(),
                ipAddress,
                "内部用户登录成功"
        );

        return new LoginResult(token, user.getId(), user.getName(), user.getRole().name(), user.isFirstLogin());
    }

    private LoginResult handleSupplierLogin(LoginCommand cmd, String ipAddress) {
        // 1. 查找用户
        SupplierUser user = supplierUserRepository.findByPhone(cmd.phone())
                .orElseThrow(() -> {
                    auditLogService.recordFailure(
                            AuditEventType.LOGIN_FAILURE,
                            null, null, null, null,
                            ipAddress,
                            "供应商手机号不存在: " + cmd.phone()
                    );
                    return new AuthenticationException("手机号或密码错误");
                });

        // 2. 检查锁定状态
        if (user.isLocked()) {
            // 检查是否满足自动解锁条件
            if (!lockoutDomainService.checkAndAutoUnlock(user)) {
                auditLogService.recordFailure(
                        AuditEventType.LOGIN_FAILURE,
                        null, null, user.getId(), user.getName(),
                        ipAddress,
                        "供应商账号已锁定"
                );
                throw new AccountLockedException("账号已锁定，请30分钟后重试或联系管理员");
            }
            // 自动解锁成功，保存状态
            supplierUserRepository.save(user);
        }

        // 3. 检查账号状态
        if (UserStatus.DISABLED == user.getStatus()) {
            auditLogService.recordFailure(
                    AuditEventType.LOGIN_FAILURE,
                    null, null, user.getId(), user.getName(),
                    ipAddress,
                    "供应商账号已停用"
            );
            throw new AuthenticationException(AuthErrorCode.ACCOUNT_DISABLED, "账号已停用，请联系管理员");
        }

        // 4. 验证密码（authenticate 成功会将 isFirstLogin 翻转为 false，故先捕获）
        boolean wasFirstLogin = user.isFirstLogin();
        boolean authenticated = user.authenticate(cmd.password(), passwordEncoder, lockoutDomainService.policy());
        if (!authenticated) {
            // 保存用户（持久化 failedAttempts 变更）
            supplierUserRepository.save(user);

            // 检查是否因此次失败被锁定
            if (user.isLocked()) {
                auditLogService.recordSuccess(
                        AuditEventType.ACCOUNT_LOCKED,
                        null, null, user.getId(), user.getName(),
                        ipAddress,
                        "供应商连续登录失败达到阈值，账号已锁定"
                );
            }

            auditLogService.recordFailure(
                    AuditEventType.LOGIN_FAILURE,
                    null, null, user.getId(), user.getName(),
                    ipAddress,
                    "供应商密码验证失败"
            );
            throw new AuthenticationException("手机号或密码错误");
        }

        // 5. 登录成功：保存用户（重置 failedAttempts）
        supplierUserRepository.save(user);

        // 5.1 首次登录：发布事件，供模块 02 将供应商流转为「待完善信息」（Req 7.3）
        if (wasFirstLogin && user.getSupplierId() != null) {
            eventPublisher.publishEvent(new SupplierFirstLoginEvent(user.getSupplierId()));
        }

        // 6. 生成 Token
        String token = tokenPort.generateToken(
                user.getId(),
                AuthConstants.USER_TYPE_SUPPLIER,
                UserRole.SUPPLIER.name(),
                user.getName()
        );

        // 7. 记录审计日志
        auditLogService.recordSuccess(
                AuditEventType.LOGIN_SUCCESS,
                user.getId(), user.getName(), user.getId(), user.getName(),
                ipAddress,
                "供应商用户登录成功"
        );

        return new LoginResult(token, user.getId(), user.getName(), UserRole.SUPPLIER.name(), user.isFirstLogin());
    }

    /**
     * JIT Provisioning：首次 SSO 登录时创建内部用户。
     */
    private InternalUser createSsoUser(SamlUserAttributes attributes) {
        InternalUser newUser = InternalUser.builder()
                .name(attributes.name())
                .email(attributes.email())
                .phone(attributes.phone())
                .ssoSubjectId(attributes.subjectId())
                .role(UserRole.BUSINESS_USER)
                .status(UserStatus.ACTIVE)
                .isSuperAdmin(false)
                .isFirstLogin(true)
                .failedAttempts(0)
                .build();

        return internalUserRepository.save(newUser);
    }

}
