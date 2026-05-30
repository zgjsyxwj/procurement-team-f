package com.cdp.ecosaas.procurement.auth.domain.model;

import com.cdp.ecosaas.procurement.auth.domain.port.PasswordEncoderPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * InternalUser 聚合根单元测试
 */
class InternalUserTests {

    private PasswordEncoderPort passwordEncoder;
    private BCryptPasswordEncoder bcrypt;
    private static final String RAW_PASSWORD = "Test@1234";
    private static final LockoutPolicy POLICY = new LockoutPolicy(5, Duration.ofMinutes(30));

    @BeforeEach
    void setUp() {
        bcrypt = new BCryptPasswordEncoder();
        passwordEncoder = new PasswordEncoderPort() {
            @Override
            public String encode(String rawPassword) {
                return bcrypt.encode(rawPassword);
            }

            @Override
            public boolean matches(String rawPassword, String encodedPassword) {
                return bcrypt.matches(rawPassword, encodedPassword);
            }
        };
    }

    private InternalUser createDefaultUser() {
        return InternalUser.builder()
                .id(1L)
                .name("测试用户")
                .phone("13800138000")
                .email("test@example.com")
                .passwordHash(bcrypt.encode(RAW_PASSWORD))
                .role(UserRole.BUYER)
                .status(UserStatus.ACTIVE)
                .isSuperAdmin(false)
                .isFirstLogin(true)
                .failedAttempts(0)
                .build();
    }

    private InternalUser createSuperAdmin() {
        return InternalUser.builder()
                .id(1L)
                .name("超级管理员")
                .phone("13900139000")
                .email("admin@example.com")
                .passwordHash(bcrypt.encode(RAW_PASSWORD))
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .isSuperAdmin(true)
                .isFirstLogin(false)
                .failedAttempts(0)
                .build();
    }

    @Nested
    @DisplayName("authenticate - 密码验证")
    class AuthenticateTests {

        @Test
        @DisplayName("正确密码应验证通过并重置失败计数")
        void shouldAuthenticateWithCorrectPassword() {
            InternalUser user = createDefaultUser();

            boolean result = user.authenticate(RAW_PASSWORD, passwordEncoder, POLICY);

            assertTrue(result);
            assertEquals(0, user.getFailedAttempts());
            assertFalse(user.isFirstLogin());
        }

        @Test
        @DisplayName("错误密码应验证失败并递增失败计数")
        void shouldFailWithWrongPassword() {
            InternalUser user = createDefaultUser();

            boolean result = user.authenticate("WrongPassword1!", passwordEncoder, POLICY);

            assertFalse(result);
            assertEquals(1, user.getFailedAttempts());
        }

        @Test
        @DisplayName("连续5次失败应触发锁定")
        void shouldLockAfterFiveFailedAttempts() {
            InternalUser user = createDefaultUser();

            for (int i = 0; i < 5; i++) {
                user.authenticate("WrongPassword1!", passwordEncoder, POLICY);
            }

            assertEquals(5, user.getFailedAttempts());
            assertTrue(user.isLocked());
        }

        @Test
        @DisplayName("锁定状态下应拒绝登录")
        void shouldRejectAuthenticationWhenLocked() {
            InternalUser user = createDefaultUser();
            user.lock(POLICY);

            boolean result = user.authenticate(RAW_PASSWORD, passwordEncoder, POLICY);

            assertFalse(result);
        }

        @Test
        @DisplayName("成功登录后应重置失败计数")
        void shouldResetFailedAttemptsOnSuccess() {
            InternalUser user = InternalUser.builder()
                    .id(1L)
                    .name("测试用户")
                    .passwordHash(bcrypt.encode(RAW_PASSWORD))
                    .role(UserRole.BUYER)
                    .status(UserStatus.ACTIVE)
                    .failedAttempts(3)
                    .build();

            boolean result = user.authenticate(RAW_PASSWORD, passwordEncoder, POLICY);

            assertTrue(result);
            assertEquals(0, user.getFailedAttempts());
        }
    }

    @Nested
    @DisplayName("lock / unlock / isLocked - 锁定逻辑")
    class LockTests {

        @Test
        @DisplayName("lock 应设置 lockedUntil 为锁定策略时长之后")
        void shouldSetLockedUntilOnLock() {
            InternalUser user = createDefaultUser();
            LocalDateTime before = LocalDateTime.now().plusMinutes(29);

            user.lock(POLICY);

            assertNotNull(user.getLockedUntil());
            assertTrue(user.getLockedUntil().isAfter(before));
            assertTrue(user.isLocked());
        }

        @Test
        @DisplayName("unlock 应清除锁定状态和失败计数")
        void shouldClearLockOnUnlock() {
            InternalUser user = createDefaultUser();
            user.lock(POLICY);

            user.unlock();

            assertNull(user.getLockedUntil());
            assertEquals(0, user.getFailedAttempts());
            assertFalse(user.isLocked());
        }

        @Test
        @DisplayName("lockedUntil 在过去时 isLocked 应返回 false")
        void shouldNotBeLockedWhenLockExpired() {
            InternalUser user = InternalUser.builder()
                    .id(1L)
                    .name("测试用户")
                    .passwordHash(bcrypt.encode(RAW_PASSWORD))
                    .role(UserRole.BUYER)
                    .status(UserStatus.ACTIVE)
                    .lockedUntil(LocalDateTime.now().minusMinutes(1))
                    .build();

            assertFalse(user.isLocked());
        }

        @Test
        @DisplayName("lockedUntil 为 null 时 isLocked 应返回 false")
        void shouldNotBeLockedWhenLockedUntilIsNull() {
            InternalUser user = createDefaultUser();

            assertFalse(user.isLocked());
        }
    }

    @Nested
    @DisplayName("changePassword - 修改密码")
    class ChangePasswordTests {

        @Test
        @DisplayName("旧密码正确时应成功修改密码")
        void shouldChangePasswordWithCorrectOldPassword() {
            InternalUser user = createDefaultUser();
            String newPassword = "NewPass@5678";

            user.changePassword(RAW_PASSWORD, newPassword, passwordEncoder);

            assertTrue(bcrypt.matches(newPassword, user.getPasswordHash()));
            assertFalse(user.isFirstLogin());
        }

        @Test
        @DisplayName("旧密码错误时应抛出异常")
        void shouldThrowWhenOldPasswordIsWrong() {
            InternalUser user = createDefaultUser();

            assertThrows(IllegalArgumentException.class, () ->
                    user.changePassword("WrongOld@123", "NewPass@5678", passwordEncoder));
        }
    }

    @Nested
    @DisplayName("changeRole - 修改角色")
    class ChangeRoleTests {

        @Test
        @DisplayName("普通用户应可以修改角色")
        void shouldChangeRoleForNormalUser() {
            InternalUser user = createDefaultUser();

            user.changeRole(UserRole.ADMIN);

            assertEquals(UserRole.ADMIN, user.getRole());
        }

        @Test
        @DisplayName("超级管理员不可修改角色")
        void shouldThrowWhenChangingSuperAdminRole() {
            InternalUser user = createSuperAdmin();

            assertThrows(IllegalStateException.class, () ->
                    user.changeRole(UserRole.BUYER));
        }
    }

    @Nested
    @DisplayName("disable / enable - 停用/启用")
    class StatusTests {

        @Test
        @DisplayName("普通用户应可以被停用")
        void shouldDisableNormalUser() {
            InternalUser user = createDefaultUser();

            user.disable();

            assertEquals(UserStatus.DISABLED, user.getStatus());
        }

        @Test
        @DisplayName("超级管理员不可被停用")
        void shouldThrowWhenDisablingSuperAdmin() {
            InternalUser user = createSuperAdmin();

            assertThrows(IllegalStateException.class, user::disable);
        }

        @Test
        @DisplayName("停用的用户应可以被启用")
        void shouldEnableDisabledUser() {
            InternalUser user = InternalUser.builder()
                    .id(1L)
                    .name("测试用户")
                    .role(UserRole.BUYER)
                    .status(UserStatus.DISABLED)
                    .build();

            user.enable();

            assertEquals(UserStatus.ACTIVE, user.getStatus());
        }
    }
}
