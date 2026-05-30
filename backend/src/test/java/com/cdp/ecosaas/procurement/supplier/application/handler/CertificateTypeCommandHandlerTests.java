package com.cdp.ecosaas.procurement.supplier.application.handler;

import com.cdp.ecosaas.procurement.shared.exception.BusinessException;
import com.cdp.ecosaas.procurement.supplier.application.command.ChangeCertTypeStatusCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.SaveCertTypeCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.UpdateCertTypeFieldsCommand;
import com.cdp.ecosaas.procurement.supplier.domain.model.CertTypeField;
import com.cdp.ecosaas.procurement.supplier.domain.model.CertificateType;
import com.cdp.ecosaas.procurement.supplier.domain.model.CertificateTypeStatus;
import com.cdp.ecosaas.procurement.supplier.domain.repository.CertificateTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CertificateTypeCommandHandler 单元测试 —— 证件类型字典维护（Req 11.1-11.5）。
 */
class CertificateTypeCommandHandlerTests {

    private final CertificateTypeRepository repository = mock(CertificateTypeRepository.class);
    private final CertificateTypeCommandHandler handler = new CertificateTypeCommandHandler(repository);

    private CertificateType existing(Long id, String name, CertificateTypeStatus status) {
        return CertificateType.builder().id(id).name(name).status(status)
                .fields(List.of(CertTypeField.builder().fieldKey("no").fieldLabel("证件编号").fieldType("TEXT").build()))
                .build();
    }

    @Test
    @DisplayName("新增证件类型：名称唯一时以启用状态保存")
    void shouldCreateCertTypeWhenNameUnique() {
        when(repository.existsByName("营业执照")).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        handler.handleSave(new SaveCertTypeCommand(null, "营业执照", "工商执照"));

        ArgumentCaptor<CertificateType> captor = ArgumentCaptor.forClass(CertificateType.class);
        verify(repository).save(captor.capture());
        assertEquals("营业执照", captor.getValue().getName());
        assertEquals(CertificateTypeStatus.ACTIVE, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("新增证件类型名称重复应拒绝且不保存")
    void shouldRejectCreateWhenNameDuplicate() {
        when(repository.existsByName("营业执照")).thenReturn(true);

        assertThrows(BusinessException.class, () -> handler.handleSave(new SaveCertTypeCommand(null, "营业执照", null)));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("名称为空应拒绝")
    void shouldRejectBlankName() {
        assertThrows(BusinessException.class, () -> handler.handleSave(new SaveCertTypeCommand(null, "  ", null)));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("编辑证件类型：保留状态与字段，仅更新名称/备注")
    void shouldEditCertTypePreservingStatusAndFields() {
        when(repository.findById(5L)).thenReturn(Optional.of(existing(5L, "旧名", CertificateTypeStatus.ACTIVE)));
        when(repository.existsByName("新名")).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        handler.handleSave(new SaveCertTypeCommand(5L, "新名", "改备注"));

        ArgumentCaptor<CertificateType> captor = ArgumentCaptor.forClass(CertificateType.class);
        verify(repository).save(captor.capture());
        assertEquals(5L, captor.getValue().getId());
        assertEquals("新名", captor.getValue().getName());
        assertEquals(CertificateTypeStatus.ACTIVE, captor.getValue().getStatus());
        assertEquals(1, captor.getValue().getFields().size());
    }

    @Test
    @DisplayName("编辑改名为已存在的名称应拒绝")
    void shouldRejectEditWhenRenamingToExistingName() {
        when(repository.findById(5L)).thenReturn(Optional.of(existing(5L, "旧名", CertificateTypeStatus.ACTIVE)));
        when(repository.existsByName("占用名")).thenReturn(true);

        assertThrows(BusinessException.class, () -> handler.handleSave(new SaveCertTypeCommand(5L, "占用名", null)));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("维护差异化字段：整体替换字段定义")
    void shouldUpdateFields() {
        when(repository.findById(5L)).thenReturn(Optional.of(existing(5L, "营业执照", CertificateTypeStatus.ACTIVE)));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<CertTypeField> newFields = List.of(
                CertTypeField.builder().fieldKey("a").fieldLabel("字段A").fieldType("TEXT").build(),
                CertTypeField.builder().fieldKey("b").fieldLabel("字段B").fieldType("DATE").build());
        handler.handleUpdateFields(new UpdateCertTypeFieldsCommand(5L, newFields));

        ArgumentCaptor<CertificateType> captor = ArgumentCaptor.forClass(CertificateType.class);
        verify(repository).save(captor.capture());
        assertEquals(2, captor.getValue().getFields().size());
        assertEquals("字段B", captor.getValue().getFields().get(1).getFieldLabel());
    }

    @Test
    @DisplayName("停用证件类型：状态置为停用")
    void shouldDisableCertType() {
        when(repository.findById(5L)).thenReturn(Optional.of(existing(5L, "营业执照", CertificateTypeStatus.ACTIVE)));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        handler.handleChangeStatus(new ChangeCertTypeStatusCommand(5L, false));

        ArgumentCaptor<CertificateType> captor = ArgumentCaptor.forClass(CertificateType.class);
        verify(repository).save(captor.capture());
        assertEquals(CertificateTypeStatus.DISABLED, captor.getValue().getStatus());
    }
}
