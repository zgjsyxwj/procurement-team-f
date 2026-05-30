package com.cdp.ecosaas.procurement.auth.application.handler;

import com.cdp.ecosaas.procurement.auth.application.command.LoginCommand;
import com.cdp.ecosaas.procurement.auth.application.service.AuditLogService;
import com.cdp.ecosaas.procurement.auth.domain.model.LockoutPolicy;
import com.cdp.ecosaas.procurement.auth.domain.model.SupplierUser;
import com.cdp.ecosaas.procurement.auth.domain.model.UserStatus;
import com.cdp.ecosaas.procurement.auth.domain.port.PasswordEncoderPort;
import com.cdp.ecosaas.procurement.auth.domain.port.SamlPort;
import com.cdp.ecosaas.procurement.auth.domain.port.TokenPort;
import com.cdp.ecosaas.procurement.auth.domain.repository.InternalUserRepository;
import com.cdp.ecosaas.procurement.auth.domain.repository.SupplierUserRepository;
import com.cdp.ecosaas.procurement.auth.domain.service.LockoutDomainService;
import com.cdp.ecosaas.procurement.auth.shared.constants.AuthConstants;
import com.cdp.ecosaas.procurement.shared.event.SupplierFirstLoginEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Duration;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AuthCommandHandler 供应商首登事件发布单元测试（Req 7.3 触发点）。
 */
class AuthCommandHandlerSupplierFirstLoginTests {

    private final InternalUserRepository internalUserRepository = mock(InternalUserRepository.class);
    private final SupplierUserRepository supplierUserRepository = mock(SupplierUserRepository.class);
    private final TokenPort tokenPort = mock(TokenPort.class);
    private final SamlPort samlPort = mock(SamlPort.class);
    private final LockoutDomainService lockoutDomainService =
            new LockoutDomainService(new LockoutPolicy(5, Duration.ofMinutes(30)));
    private final AuditLogService auditLogService = mock(AuditLogService.class);
    private final PasswordEncoderPort passwordEncoder = mock(PasswordEncoderPort.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

    private final AuthCommandHandler handler = new AuthCommandHandler(
            internalUserRepository, supplierUserRepository, tokenPort, samlPort,
            lockoutDomainService, auditLogService, passwordEncoder, eventPublisher);

    private SupplierUser supplierUser(boolean firstLogin, Long supplierId) {
        return SupplierUser.builder().id(2L).name("供应商用户").phone("13800138000")
                .passwordHash("hash").supplierId(supplierId).status(UserStatus.ACTIVE)
                .isFirstLogin(firstLogin).failedAttempts(0).build();
    }

    private void stubSuccessfulAuth(SupplierUser user) {
        when(supplierUserRepository.findByPhone("13800138000")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pwd", "hash")).thenReturn(true);
        when(tokenPort.generateToken(any(), any(), any(), any())).thenReturn("token");
    }

    @Test
    @DisplayName("供应商首次登录成功：发布 SupplierFirstLoginEvent（携带 supplierId）")
    void shouldPublishEventOnSupplierFirstLogin() {
        stubSuccessfulAuth(supplierUser(true, 55L));

        handler.handleLogin(new LoginCommand("13800138000", "pwd", AuthConstants.USER_TYPE_SUPPLIER), "1.2.3.4");

        verify(eventPublisher).publishEvent(new SupplierFirstLoginEvent(55L));
    }

    @Test
    @DisplayName("供应商非首次登录：不发布首登事件")
    void shouldNotPublishEventOnNonFirstLogin() {
        stubSuccessfulAuth(supplierUser(false, 55L));

        handler.handleLogin(new LoginCommand("13800138000", "pwd", AuthConstants.USER_TYPE_SUPPLIER), "1.2.3.4");

        verify(eventPublisher, never()).publishEvent(any());
    }
}
