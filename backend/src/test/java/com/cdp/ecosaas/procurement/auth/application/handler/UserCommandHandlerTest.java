package com.cdp.ecosaas.procurement.auth.application.handler;

import com.cdp.ecosaas.procurement.auth.application.command.CreateInternalUserCommand;
import com.cdp.ecosaas.procurement.auth.application.command.UnlockUserCommand;
import com.cdp.ecosaas.procurement.auth.application.command.UpdateUserRoleCommand;
import com.cdp.ecosaas.procurement.auth.application.command.UpdateUserStatusCommand;
import com.cdp.ecosaas.procurement.auth.application.service.AuditLogService;
import com.cdp.ecosaas.procurement.auth.domain.model.InternalUser;
import com.cdp.ecosaas.procurement.auth.domain.model.UserRole;
import com.cdp.ecosaas.procurement.auth.domain.model.UserStatus;
import com.cdp.ecosaas.procurement.auth.domain.port.EmailPort;
import com.cdp.ecosaas.procurement.auth.domain.port.PasswordEncoderPort;
import com.cdp.ecosaas.procurement.auth.domain.repository.InternalUserRepository;
import com.cdp.ecosaas.procurement.auth.domain.repository.PasswordHistoryRepository;
import com.cdp.ecosaas.procurement.auth.domain.service.PasswordDomainService;
import com.cdp.ecosaas.procurement.auth.shared.enums.AuditEventType;
import com.cdp.ecosaas.procurement.auth.shared.exception.AuthErrorCode;
import com.cdp.ecosaas.procurement.auth.shared.exception.AuthenticationException;
import com.cdp.ecosaas.procurement.shared.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * UserCommandHandler 单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserCommandHandlerTest {

    @Mock
    private InternalUserRepository internalUserRepository;

    @Mock
    private PasswordDomainService passwordDomainService;

    @Mock
    private PasswordHistoryRepository passwordHistoryRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private EmailPort emailPort;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @InjectMocks
    private UserCommandHandler userCommandHandler;

    private static final Long OPERATOR_ID = 1L;
    private static final String IP_ADDRESS = "192.168.1.1";

    @Nested
    @DisplayName("handleCreateUser - 创建内部用户")
    class CreateUserTests {

        @Test
        @DisplayName("应成功创建用户并发送初始密码邮件")
        void shouldCreateUserSuccessfully() {
            CreateInternalUserCommand cmd = new CreateInternalUserCommand(
                    "张三", "13800138001", "zhangsan@example.com", "BUYER"
            );

            when(internalUserRepository.findByPhone("13800138001")).thenReturn(Optional.empty());
            when(internalUserRepository.findByEmail("zhangsan@example.com")).thenReturn(Optional.empty());
            when(passwordDomainService.generateRandomPassword()).thenReturn("RandomPass@123");
            when(passwordDomainService.hashPassword(eq("RandomPass@123"), any())).thenReturn("$2a$hashed");
            when(internalUserRepository.save(any(InternalUser.class))).thenAnswer(invocation -> {
                InternalUser user = invocation.getArgument(0);
                return InternalUser.builder()
                        .id(10L)
                        .name(user.getName())
                        .phone(user.getPhone())
                        .email(user.getEmail())
                        .passwordHash(user.getPasswordHash())
                        .role(user.getRole())
                        .status(user.getStatus())
                        .isSuperAdmin(user.isSuperAdmin())
                        .isFirstLogin(user.isFirstLogin())
                        .failedAttempts(user.getFailedAttempts())
                        .build();
            });

            Long userId = userCommandHandler.handleCreateUser(cmd, OPERATOR_ID, IP_ADDRESS);

            assertEquals(10L, userId);
            verify(emailPort).sendInitialPassword("zhangsan@example.com", "张三", "RandomPass@123");
            verify(passwordHistoryRepository).save(any());
            verify(auditLogService).recordSuccess(eq(AuditEventType.ACCOUNT_CREATED), eq(OPERATOR_ID), any(), eq(10L), any(), eq(IP_ADDRESS), any());
        }

        @Test
        @DisplayName("手机号已存在时应抛出异常")
        void shouldThrowWhenPhoneExists() {
            CreateInternalUserCommand cmd = new CreateInternalUserCommand(
                    "张三", "13800138001", "zhangsan@example.com", "BUYER"
            );

            InternalUser existingUser = InternalUser.builder().id(5L).build();
            when(internalUserRepository.findByPhone("13800138001")).thenReturn(Optional.of(existingUser));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    userCommandHandler.handleCreateUser(cmd, OPERATOR_ID, IP_ADDRESS));
            assertEquals(AuthErrorCode.PHONE_ALREADY_USED.getCode(), ex.getErrorCode());
        }

        @Test
        @DisplayName("邮箱已存在时应抛出异常")
        void shouldThrowWhenEmailExists() {
            CreateInternalUserCommand cmd = new CreateInternalUserCommand(
                    "张三", "13800138001", "zhangsan@example.com", "BUYER"
            );

            when(internalUserRepository.findByPhone("13800138001")).thenReturn(Optional.empty());
            InternalUser existingUser = InternalUser.builder().id(5L).build();
            when(internalUserRepository.findByEmail("zhangsan@example.com")).thenReturn(Optional.of(existingUser));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    userCommandHandler.handleCreateUser(cmd, OPERATOR_ID, IP_ADDRESS));
            assertEquals(AuthErrorCode.EMAIL_ALREADY_USED.getCode(), ex.getErrorCode());
        }

        @Test
        @DisplayName("手机号为空时应跳过手机号唯一性校验")
        void shouldSkipPhoneCheckWhenPhoneIsNull() {
            CreateInternalUserCommand cmd = new CreateInternalUserCommand(
                    "张三", null, "zhangsan@example.com", "BUYER"
            );

            when(internalUserRepository.findByEmail("zhangsan@example.com")).thenReturn(Optional.empty());
            when(passwordDomainService.generateRandomPassword()).thenReturn("RandomPass@123");
            when(passwordDomainService.hashPassword(eq("RandomPass@123"), any())).thenReturn("$2a$hashed");
            when(internalUserRepository.save(any(InternalUser.class))).thenAnswer(invocation -> {
                InternalUser user = invocation.getArgument(0);
                return InternalUser.builder()
                        .id(10L)
                        .name(user.getName())
                        .email(user.getEmail())
                        .passwordHash(user.getPasswordHash())
                        .role(user.getRole())
                        .status(user.getStatus())
                        .isSuperAdmin(false)
                        .isFirstLogin(true)
                        .failedAttempts(0)
                        .build();
            });

            Long userId = userCommandHandler.handleCreateUser(cmd, OPERATOR_ID, IP_ADDRESS);

            assertEquals(10L, userId);
            verify(internalUserRepository, never()).findByPhone(any());
        }
    }

    @Nested
    @DisplayName("handleUpdateRole - 修改用户角色")
    class UpdateRoleTests {

        @Test
        @DisplayName("应成功修改普通用户角色")
        void shouldUpdateRoleSuccessfully() {
            UpdateUserRoleCommand cmd = new UpdateUserRoleCommand(2L, "ADMIN");

            InternalUser user = InternalUser.builder()
                    .id(2L)
                    .name("李四")
                    .role(UserRole.BUYER)
                    .status(UserStatus.ACTIVE)
                    .isSuperAdmin(false)
                    .build();

            when(internalUserRepository.findById(2L)).thenReturn(Optional.of(user));
            when(internalUserRepository.save(any(InternalUser.class))).thenReturn(user);

            userCommandHandler.handleUpdateRole(cmd, OPERATOR_ID, IP_ADDRESS);

            assertEquals(UserRole.ADMIN, user.getRole());
            verify(internalUserRepository).save(user);
            verify(auditLogService).recordSuccess(eq(AuditEventType.ROLE_CHANGED), eq(OPERATOR_ID), any(), eq(2L), any(), eq(IP_ADDRESS), any());
        }

        @Test
        @DisplayName("超级管理员角色不可变更")
        void shouldThrowWhenChangingSuperAdminRole() {
            UpdateUserRoleCommand cmd = new UpdateUserRoleCommand(2L, "BUYER");

            InternalUser superAdmin = InternalUser.builder()
                    .id(2L)
                    .name("超级管理员")
                    .role(UserRole.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .isSuperAdmin(true)
                    .build();

            when(internalUserRepository.findById(2L)).thenReturn(Optional.of(superAdmin));

            assertThrows(IllegalStateException.class, () ->
                    userCommandHandler.handleUpdateRole(cmd, OPERATOR_ID, IP_ADDRESS));
        }

        @Test
        @DisplayName("用户不存在时应抛出异常")
        void shouldThrowWhenUserNotFound() {
            UpdateUserRoleCommand cmd = new UpdateUserRoleCommand(999L, "ADMIN");

            when(internalUserRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(AuthenticationException.class, () ->
                    userCommandHandler.handleUpdateRole(cmd, OPERATOR_ID, IP_ADDRESS));
        }
    }

    @Nested
    @DisplayName("handleUpdateStatus - 停用/启用用户")
    class UpdateStatusTests {

        @Test
        @DisplayName("应成功停用普通用户")
        void shouldDisableUserSuccessfully() {
            UpdateUserStatusCommand cmd = new UpdateUserStatusCommand(2L, "DISABLED");

            InternalUser user = InternalUser.builder()
                    .id(2L)
                    .name("李四")
                    .role(UserRole.BUYER)
                    .status(UserStatus.ACTIVE)
                    .isSuperAdmin(false)
                    .build();

            when(internalUserRepository.findById(2L)).thenReturn(Optional.of(user));
            when(internalUserRepository.save(any(InternalUser.class))).thenReturn(user);

            userCommandHandler.handleUpdateStatus(cmd, OPERATOR_ID, IP_ADDRESS);

            assertEquals(UserStatus.DISABLED, user.getStatus());
            verify(auditLogService).recordSuccess(eq(AuditEventType.ACCOUNT_DISABLED), eq(OPERATOR_ID), any(), eq(2L), any(), eq(IP_ADDRESS), any());
        }

        @Test
        @DisplayName("应成功启用已停用用户")
        void shouldEnableUserSuccessfully() {
            UpdateUserStatusCommand cmd = new UpdateUserStatusCommand(2L, "ACTIVE");

            InternalUser user = InternalUser.builder()
                    .id(2L)
                    .name("李四")
                    .role(UserRole.BUYER)
                    .status(UserStatus.DISABLED)
                    .isSuperAdmin(false)
                    .build();

            when(internalUserRepository.findById(2L)).thenReturn(Optional.of(user));
            when(internalUserRepository.save(any(InternalUser.class))).thenReturn(user);

            userCommandHandler.handleUpdateStatus(cmd, OPERATOR_ID, IP_ADDRESS);

            assertEquals(UserStatus.ACTIVE, user.getStatus());
            verify(auditLogService).recordSuccess(eq(AuditEventType.ACCOUNT_ENABLED), eq(OPERATOR_ID), any(), eq(2L), any(), eq(IP_ADDRESS), any());
        }

        @Test
        @DisplayName("超级管理员不可被停用")
        void shouldThrowWhenDisablingSuperAdmin() {
            UpdateUserStatusCommand cmd = new UpdateUserStatusCommand(2L, "DISABLED");

            InternalUser superAdmin = InternalUser.builder()
                    .id(2L)
                    .name("超级管理员")
                    .role(UserRole.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .isSuperAdmin(true)
                    .build();

            when(internalUserRepository.findById(2L)).thenReturn(Optional.of(superAdmin));

            assertThrows(IllegalStateException.class, () ->
                    userCommandHandler.handleUpdateStatus(cmd, OPERATOR_ID, IP_ADDRESS));
        }
    }

    @Nested
    @DisplayName("handleUnlock - 手动解锁")
    class UnlockTests {

        @Test
        @DisplayName("应成功解锁被锁定的用户")
        void shouldUnlockUserSuccessfully() {
            UnlockUserCommand cmd = new UnlockUserCommand(2L);

            InternalUser user = InternalUser.builder()
                    .id(2L)
                    .name("李四")
                    .role(UserRole.BUYER)
                    .status(UserStatus.ACTIVE)
                    .isSuperAdmin(false)
                    .failedAttempts(5)
                    .lockedUntil(LocalDateTime.now().plusMinutes(20))
                    .build();

            when(internalUserRepository.findById(2L)).thenReturn(Optional.of(user));
            when(internalUserRepository.save(any(InternalUser.class))).thenReturn(user);

            userCommandHandler.handleUnlock(cmd, OPERATOR_ID, IP_ADDRESS);

            assertEquals(0, user.getFailedAttempts());
            assertNull(user.getLockedUntil());
            assertFalse(user.isLocked());
            verify(auditLogService).recordSuccess(eq(AuditEventType.ACCOUNT_UNLOCKED), eq(OPERATOR_ID), any(), eq(2L), any(), eq(IP_ADDRESS), any());
        }

        @Test
        @DisplayName("用户不存在时应抛出异常")
        void shouldThrowWhenUserNotFound() {
            UnlockUserCommand cmd = new UnlockUserCommand(999L);

            when(internalUserRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(AuthenticationException.class, () ->
                    userCommandHandler.handleUnlock(cmd, OPERATOR_ID, IP_ADDRESS));
        }
    }
}
