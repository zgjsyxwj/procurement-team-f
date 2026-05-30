package com.cdp.ecosaas.procurement.supplier.application.handler;

import com.cdp.ecosaas.procurement.shared.exception.BusinessException;
import com.cdp.ecosaas.procurement.supplier.application.command.CreateSupplierCommand;
import com.cdp.ecosaas.procurement.supplier.domain.model.Supplier;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierCategory;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierContact;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierInvitationLog;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierStatus;
import com.cdp.ecosaas.procurement.supplier.domain.port.BuyerSupplierRelationPort;
import com.cdp.ecosaas.procurement.supplier.domain.port.EmailPort;
import com.cdp.ecosaas.procurement.supplier.domain.port.SupplierAccountPort;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierContactRepository;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierInvitationLogRepository;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierRepository;
import com.cdp.ecosaas.procurement.supplier.domain.service.SupplierCodeGenerator;
import com.cdp.ecosaas.procurement.supplier.domain.service.SupplierLifecycleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * SupplierCommandHandler 单元测试 —— 供应商创建与（创建时）邀请（Req 6.1-6.6、6.8、7.2）。
 */
class SupplierCommandHandlerTests {

    private final SupplierRepository supplierRepository = mock(SupplierRepository.class);
    private final SupplierContactRepository contactRepository = mock(SupplierContactRepository.class);
    private final SupplierInvitationLogRepository invitationLogRepository = mock(SupplierInvitationLogRepository.class);
    private final SupplierAccountPort accountPort = mock(SupplierAccountPort.class);
    private final BuyerSupplierRelationPort relationPort = mock(BuyerSupplierRelationPort.class);
    private final EmailPort emailPort = mock(EmailPort.class);

    private final SupplierCommandHandler handler = new SupplierCommandHandler(
            supplierRepository, contactRepository, invitationLogRepository,
            new SupplierCodeGenerator(), new SupplierLifecycleService(),
            accountPort, relationPort, emailPort);

    private static final Long OPERATOR_ID = 7L;

    private CreateSupplierCommand command(boolean sendInvitation) {
        return new CreateSupplierCommand("测试供应商", SupplierCategory.DOMESTIC,
                "张三", "13800138000", "zhang@test.com", sendInvitation);
    }

    private void stubPersistence() {
        when(supplierRepository.nextCodeSequence()).thenReturn(1L);
        when(supplierRepository.save(any())).thenReturn(Supplier.builder()
                .id(1L).supplierCode("VD0001").name("测试供应商")
                .category(SupplierCategory.DOMESTIC).status(SupplierStatus.CREATED).build());
        when(contactRepository.save(any())).thenReturn(SupplierContact.builder()
                .id(10L).supplierId(1L).name("张三").phone("13800138000")
                .email("zhang@test.com").isPrimary(true).build());
        when(accountPort.createAccount(any(), any(), any(), any())).thenReturn("Init@1234");
    }

    @Test
    @DisplayName("仅保存：生成编号、存供应商+主要联系人、建号、建管理关系，状态为创建成功，不发邀请")
    void shouldCreateSupplierWithoutInvitation() {
        stubPersistence();

        Supplier result = handler.handleCreateSupplier(command(false), OPERATOR_ID);

        assertEquals("VD0001", result.getSupplierCode());
        assertEquals(SupplierStatus.CREATED, result.getStatus());
        // 主要联系人随之创建
        ArgumentCaptor<SupplierContact> contactCaptor = ArgumentCaptor.forClass(SupplierContact.class);
        verify(contactRepository).save(contactCaptor.capture());
        assertEquals(true, contactCaptor.getValue().isPrimary());
        assertEquals(1L, contactCaptor.getValue().getSupplierId());
        // 建号（手机号 + 返回初始密码）
        verify(accountPort).createAccount(1L, "张三", "13800138000", "zhang@test.com");
        // 建立管理关系 source=CREATED
        verify(relationPort).createRelation(OPERATOR_ID, 1L, "CREATED");
        // 不发邀请
        verifyNoInteractions(emailPort);
        verifyNoInteractions(invitationLogRepository);
    }

    @Test
    @DisplayName("保存并邀请：发邀请邮件（含登录手机号+初始密码）、写成功邀请日志、状态流转为待进入")
    void shouldCreateSupplierAndSendInvitation() {
        stubPersistence();

        Supplier result = handler.handleCreateSupplier(command(true), OPERATOR_ID);

        assertEquals(SupplierStatus.PENDING_ENTRY, result.getStatus());
        verify(emailPort).sendSupplierInvitation("zhang@test.com", "张三", "测试供应商", "13800138000", "Init@1234");
        ArgumentCaptor<SupplierInvitationLog> logCaptor = ArgumentCaptor.forClass(SupplierInvitationLog.class);
        verify(invitationLogRepository).save(logCaptor.capture());
        assertEquals("SUCCESS", logCaptor.getValue().getResult());
        assertEquals("zhang@test.com", logCaptor.getValue().getRecipientEmail());
        assertEquals(1L, logCaptor.getValue().getSupplierId());
        assertEquals(10L, logCaptor.getValue().getContactId());
        // 初始保存 + 流转后再保存
        verify(supplierRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("手机号格式非法应拒绝，且不触碰任何持久化/账号/关系")
    void shouldRejectInvalidPhone() {
        CreateSupplierCommand cmd = new CreateSupplierCommand("测试供应商", SupplierCategory.DOMESTIC,
                "张三", "not-a-phone", "zhang@test.com", false);

        assertThrows(BusinessException.class, () -> handler.handleCreateSupplier(cmd, OPERATOR_ID));

        verify(supplierRepository, never()).save(any());
        verifyNoInteractions(accountPort);
        verifyNoInteractions(relationPort);
    }

    @Test
    @DisplayName("邮箱格式非法应拒绝")
    void shouldRejectInvalidEmail() {
        CreateSupplierCommand cmd = new CreateSupplierCommand("测试供应商", SupplierCategory.DOMESTIC,
                "张三", "13800138000", "bad-email", false);

        assertThrows(BusinessException.class, () -> handler.handleCreateSupplier(cmd, OPERATOR_ID));

        verify(supplierRepository, never()).save(any());
    }
}
