package com.cdp.ecosaas.procurement.supplier.infrastructure.external;

import com.cdp.ecosaas.procurement.auth.domain.model.SupplierUser;
import com.cdp.ecosaas.procurement.auth.domain.model.UserStatus;
import com.cdp.ecosaas.procurement.auth.domain.port.PasswordEncoderPort;
import com.cdp.ecosaas.procurement.auth.domain.repository.SupplierUserRepository;
import com.cdp.ecosaas.procurement.auth.domain.service.PasswordDomainService;
import com.cdp.ecosaas.procurement.shared.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SupplierAccountAdapter 单元测试 —— 任务 17.2 接入模块 01 真实开通/停用/启用供应商登录账号。
 */
class SupplierAccountAdapterTests {

    private final SupplierUserRepository supplierUserRepository = mock(SupplierUserRepository.class);
    private final PasswordDomainService passwordDomainService = mock(PasswordDomainService.class);
    private final PasswordEncoderPort passwordEncoder = mock(PasswordEncoderPort.class);
    private final SupplierAccountAdapter adapter =
            new SupplierAccountAdapter(supplierUserRepository, passwordDomainService, passwordEncoder);

    @Test
    @DisplayName("createAccount 以手机号建账号、哈希存储随机初始密码并返回明文")
    void shouldCreateSupplierUserAndReturnInitialPassword() {
        when(supplierUserRepository.findByPhone("13778899000")).thenReturn(Optional.empty());
        when(passwordDomainService.generateRandomPassword()).thenReturn("Init@1234");
        when(passwordEncoder.encode("Init@1234")).thenReturn("HASHED");
        when(supplierUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        String initialPassword = adapter.createAccount(3L, "alice", "13778899000", "a@b.com");

        assertEquals("Init@1234", initialPassword);
        ArgumentCaptor<SupplierUser> captor = ArgumentCaptor.forClass(SupplierUser.class);
        verify(supplierUserRepository).save(captor.capture());
        SupplierUser saved = captor.getValue();
        assertEquals("alice", saved.getName());
        assertEquals("13778899000", saved.getPhone());
        assertEquals("a@b.com", saved.getEmail());
        assertEquals(3L, saved.getSupplierId());
        assertEquals("HASHED", saved.getPasswordHash());
        assertEquals(UserStatus.ACTIVE, saved.getStatus());
        assertTrue(saved.isFirstLogin());
    }

    @Test
    @DisplayName("createAccount 手机号已被占用时抛 BusinessException 且不落库（Req 6.5）")
    void shouldRejectWhenPhoneAlreadyTaken() {
        when(supplierUserRepository.findByPhone("13778899000"))
                .thenReturn(Optional.of(mock(SupplierUser.class)));

        assertThrows(BusinessException.class,
                () -> adapter.createAccount(3L, "alice", "13778899000", "a@b.com"));
        verify(supplierUserRepository, never()).save(any());
    }

    @Test
    @DisplayName("disableAccount 将供应商登录账号置为停用并落库")
    void shouldDisableSupplierUser() {
        SupplierUser existing = SupplierUser.builder()
                .id(9L).supplierId(3L).status(UserStatus.ACTIVE).build();
        when(supplierUserRepository.findBySupplierId(3L)).thenReturn(Optional.of(existing));

        adapter.disableAccount(3L);

        ArgumentCaptor<SupplierUser> captor = ArgumentCaptor.forClass(SupplierUser.class);
        verify(supplierUserRepository).save(captor.capture());
        assertEquals(UserStatus.DISABLED, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("enableAccount 将供应商登录账号置为启用并落库")
    void shouldEnableSupplierUser() {
        SupplierUser existing = SupplierUser.builder()
                .id(9L).supplierId(3L).status(UserStatus.DISABLED).build();
        when(supplierUserRepository.findBySupplierId(3L)).thenReturn(Optional.of(existing));

        adapter.enableAccount(3L);

        ArgumentCaptor<SupplierUser> captor = ArgumentCaptor.forClass(SupplierUser.class);
        verify(supplierUserRepository).save(captor.capture());
        assertEquals(UserStatus.ACTIVE, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("disableAccount 账号尚未开通时仅记日志、不落库")
    void shouldSkipDisableWhenAccountAbsent() {
        when(supplierUserRepository.findBySupplierId(3L)).thenReturn(Optional.empty());

        adapter.disableAccount(3L);

        verify(supplierUserRepository, never()).save(any());
    }
}
