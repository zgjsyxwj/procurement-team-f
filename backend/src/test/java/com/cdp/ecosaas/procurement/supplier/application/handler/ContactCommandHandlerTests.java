package com.cdp.ecosaas.procurement.supplier.application.handler;

import com.cdp.ecosaas.procurement.shared.exception.BusinessException;
import com.cdp.ecosaas.procurement.supplier.application.command.DeleteContactCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.SaveContactCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.SetPrimaryContactCommand;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierContact;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierContactRepository;
import com.cdp.ecosaas.procurement.supplier.domain.service.ContactDomainService;
import com.cdp.ecosaas.procurement.supplier.shared.exception.PrimaryContactRequiredException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ContactCommandHandler 单元测试 —— 联系人新增/编辑、设主、删除（Req 9.1、9.4、9.5、9.6、9.8）。
 */
class ContactCommandHandlerTests {

    private final SupplierContactRepository contactRepository = mock(SupplierContactRepository.class);

    private final ContactCommandHandler handler =
            new ContactCommandHandler(contactRepository, new ContactDomainService());

    private SupplierContact contact(Long id, boolean primary) {
        return SupplierContact.builder().id(id).supplierId(1L).name("联系人" + id)
                .phone("13800138000").email("c" + id + "@test.com").isPrimary(primary).build();
    }

    @Test
    @DisplayName("新增非主要联系人：直接保存，不联动其他联系人")
    void shouldCreateNonPrimaryContact() {
        when(contactRepository.save(any())).thenAnswer(inv -> {
            SupplierContact c = inv.getArgument(0);
            return SupplierContact.builder().id(20L).supplierId(c.getSupplierId()).name(c.getName())
                    .phone(c.getPhone()).email(c.getEmail()).isPrimary(c.isPrimary()).build();
        });

        SupplierContact result = handler.handleSave(new SaveContactCommand(
                1L, null, "李四", "13900139000", "li@test.com", false, "经理", "采购部"));

        ArgumentCaptor<SupplierContact> captor = ArgumentCaptor.forClass(SupplierContact.class);
        verify(contactRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getSupplierId());
        assertEquals("李四", captor.getValue().getName());
        assertFalse(captor.getValue().isPrimary());
        assertEquals(20L, result.getId());
        verify(contactRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("新增主要联系人：保存后自动取消原主要联系人标记")
    void shouldCreatePrimaryContactAndUnmarkOldPrimary() {
        when(contactRepository.save(any())).thenReturn(contact(20L, true));
        when(contactRepository.findBySupplierId(1L)).thenReturn(List.of(contact(10L, true), contact(20L, true)));

        handler.handleSave(new SaveContactCommand(
                1L, null, "新主要", "13900139000", "new@test.com", true, null, null));

        ArgumentCaptor<List<SupplierContact>> captor = ArgumentCaptor.forClass(List.class);
        verify(contactRepository).saveAll(captor.capture());
        SupplierContact old = captor.getValue().stream().filter(c -> c.getId().equals(10L)).findFirst().orElseThrow();
        SupplierContact fresh = captor.getValue().stream().filter(c -> c.getId().equals(20L)).findFirst().orElseThrow();
        assertFalse(old.isPrimary());
        assertTrue(fresh.isPrimary());
    }

    @Test
    @DisplayName("编辑联系人：基于既有记录重建并即时保存")
    void shouldUpdateExistingContact() {
        when(contactRepository.findById(10L)).thenReturn(Optional.of(contact(10L, false)));
        when(contactRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        handler.handleSave(new SaveContactCommand(
                1L, 10L, "改名", "13700137000", "edit@test.com", false, "总监", "财务部"));

        ArgumentCaptor<SupplierContact> captor = ArgumentCaptor.forClass(SupplierContact.class);
        verify(contactRepository).save(captor.capture());
        assertEquals(10L, captor.getValue().getId());
        assertEquals("改名", captor.getValue().getName());
        assertEquals("财务部", captor.getValue().getDepartment());
    }

    @Test
    @DisplayName("设为主要联系人：目标置主、其余取消主要标记")
    void shouldSetPrimaryAndUnmarkOthers() {
        when(contactRepository.findBySupplierId(1L)).thenReturn(List.of(contact(10L, true), contact(11L, false)));

        handler.handleSetPrimary(new SetPrimaryContactCommand(1L, 11L));

        ArgumentCaptor<List<SupplierContact>> captor = ArgumentCaptor.forClass(List.class);
        verify(contactRepository).saveAll(captor.capture());
        SupplierContact c10 = captor.getValue().stream().filter(c -> c.getId().equals(10L)).findFirst().orElseThrow();
        SupplierContact c11 = captor.getValue().stream().filter(c -> c.getId().equals(11L)).findFirst().orElseThrow();
        assertFalse(c10.isPrimary());
        assertTrue(c11.isPrimary());
    }

    @Test
    @DisplayName("删除唯一主要联系人应被拒绝，且不执行删除")
    void shouldRejectDeletingPrimaryContact() {
        when(contactRepository.findBySupplierId(1L)).thenReturn(List.of(contact(10L, true), contact(11L, false)));

        assertThrows(PrimaryContactRequiredException.class,
                () -> handler.handleDelete(new DeleteContactCommand(1L, 10L)));

        verify(contactRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("删除非主要联系人：正常删除")
    void shouldDeleteNonPrimaryContact() {
        when(contactRepository.findBySupplierId(1L)).thenReturn(List.of(contact(10L, true), contact(11L, false)));

        handler.handleDelete(new DeleteContactCommand(1L, 11L));

        verify(contactRepository).deleteById(11L);
    }

    @Test
    @DisplayName("联系人手机号格式非法应拒绝，且不保存")
    void shouldRejectInvalidPhone() {
        assertThrows(BusinessException.class, () -> handler.handleSave(new SaveContactCommand(
                1L, null, "李四", "bad-phone", "li@test.com", false, null, null)));

        verify(contactRepository, never()).save(any());
    }
}
