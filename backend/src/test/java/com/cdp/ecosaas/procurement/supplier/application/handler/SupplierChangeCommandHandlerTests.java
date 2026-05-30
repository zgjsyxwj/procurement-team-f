package com.cdp.ecosaas.procurement.supplier.application.handler;

import com.cdp.ecosaas.procurement.supplier.application.command.ReviewChangeCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.SubmitForReviewCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.SubmitSupplierChangeCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.UpdateSupplierInfoCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.WithdrawChangeCommand;
import com.cdp.ecosaas.procurement.supplier.domain.model.ChangeRequestStatus;
import com.cdp.ecosaas.procurement.supplier.domain.model.ChangeSource;
import com.cdp.ecosaas.procurement.supplier.domain.model.ChangeType;
import com.cdp.ecosaas.procurement.supplier.domain.model.Supplier;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierCategory;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierChangeField;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierChangeRequest;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierContact;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierStatus;
import com.cdp.ecosaas.procurement.supplier.domain.port.EmailPort;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierChangeRequestRepository;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierContactRepository;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierRepository;
import com.cdp.ecosaas.procurement.supplier.domain.service.ChangeReviewService;
import com.cdp.ecosaas.procurement.supplier.shared.exception.DuplicatePendingChangeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
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
 * SupplierChangeCommandHandler 单元测试 —— 信息编辑与变更审核（Req 3.3、3.6、3.7、4.4、5.3、5.4、49）。
 */
class SupplierChangeCommandHandlerTests {

    private final SupplierRepository supplierRepository = mock(SupplierRepository.class);
    private final SupplierContactRepository contactRepository = mock(SupplierContactRepository.class);
    private final SupplierChangeRequestRepository changeRequestRepository = mock(SupplierChangeRequestRepository.class);
    private final EmailPort emailPort = mock(EmailPort.class);

    private final SupplierChangeCommandHandler handler = new SupplierChangeCommandHandler(
            supplierRepository, contactRepository, changeRequestRepository,
            new ChangeReviewService(), emailPort);

    private Supplier supplier(Long id, SupplierStatus status, String name) {
        return Supplier.builder().id(id).supplierCode("VD0001").name(name)
                .category(SupplierCategory.DOMESTIC).status(status).legalPerson("张三").build();
    }

    private SupplierChangeRequest pendingBasicInfo(Long id, Long supplierId, String beforeName, String afterName) {
        return SupplierChangeRequest.builder()
                .id(id).supplierId(supplierId).changeType(ChangeType.BASIC_INFO)
                .source(ChangeSource.SUPPLIER).status(ChangeRequestStatus.PENDING_REVIEW)
                .fields(List.of(SupplierChangeField.builder()
                        .fieldKey("name").fieldLabel("供应商名称")
                        .beforeValue(beforeName).afterValue(afterName).build()))
                .build();
    }

    private static boolean hasChange(SupplierChangeRequest req, String key, String after) {
        return req.getFields().stream().anyMatch(f -> key.equals(f.getFieldKey()) && after.equals(f.getAfterValue()));
    }

    @Test
    @DisplayName("采购员直接编辑：即时生效并记录一条已通过(BUYER)变更，仅含改动字段")
    void shouldApplyBuyerEditImmediatelyAndRecordApprovedChange() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier(1L, SupplierStatus.ACTIVE, "老名")));
        when(supplierRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        handler.handleBuyerEdit(new UpdateSupplierInfoCommand(1L, Map.of("name", "新名", "legalPerson", "李四")),
                7L, "采购员A");

        ArgumentCaptor<Supplier> supplierCaptor = ArgumentCaptor.forClass(Supplier.class);
        verify(supplierRepository).save(supplierCaptor.capture());
        assertEquals("新名", supplierCaptor.getValue().getName());
        assertEquals("李四", supplierCaptor.getValue().getLegalPerson());

        ArgumentCaptor<SupplierChangeRequest> reqCaptor = ArgumentCaptor.forClass(SupplierChangeRequest.class);
        verify(changeRequestRepository).save(reqCaptor.capture());
        SupplierChangeRequest req = reqCaptor.getValue();
        assertEquals(ChangeSource.BUYER, req.getSource());
        assertEquals(ChangeRequestStatus.APPROVED, req.getStatus());
        assertEquals(7L, req.getSubmitterId());
        assertEquals(2, req.getFields().size());
        assertTrue(hasChange(req, "name", "新名"));
        assertTrue(hasChange(req, "legalPerson", "李四"));
    }

    @Test
    @DisplayName("供应商合作中提交变更：落待审核(SUPPLIER)变更，不立即改动供应商主表")
    void shouldCreatePendingChangeWhenActiveSupplierSubmits() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier(1L, SupplierStatus.ACTIVE, "老名")));
        when(changeRequestRepository.findPendingBySupplierId(1L)).thenReturn(List.of());

        handler.handleSupplierSubmit(
                new SubmitSupplierChangeCommand(1L, ChangeType.BASIC_INFO, Map.of("name", "新名")), 99L, "联系人");

        verify(supplierRepository, never()).save(any());
        ArgumentCaptor<SupplierChangeRequest> reqCaptor = ArgumentCaptor.forClass(SupplierChangeRequest.class);
        verify(changeRequestRepository).save(reqCaptor.capture());
        SupplierChangeRequest req = reqCaptor.getValue();
        assertEquals(ChangeSource.SUPPLIER, req.getSource());
        assertEquals(ChangeRequestStatus.PENDING_REVIEW, req.getStatus());
        assertEquals(99L, req.getSubmitterId());
        assertTrue(hasChange(req, "name", "新名"));
    }

    @Test
    @DisplayName("合作中已存在同类待审核变更时再提交应被拒绝")
    void shouldRejectWhenDuplicatePendingChange() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier(1L, SupplierStatus.ACTIVE, "老名")));
        when(changeRequestRepository.findPendingBySupplierId(1L))
                .thenReturn(List.of(pendingBasicInfo(5L, 1L, "老名", "其它名")));

        assertThrows(DuplicatePendingChangeException.class, () -> handler.handleSupplierSubmit(
                new SubmitSupplierChangeCommand(1L, ChangeType.BASIC_INFO, Map.of("name", "新名")), 99L, "联系人"));

        verify(changeRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("待完善信息(草稿)阶段供应商编辑直接生效，不落变更记录")
    void shouldApplyDraftDirectlyWhenPendingInfo() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier(1L, SupplierStatus.PENDING_INFO, "老名")));
        when(supplierRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        handler.handleSupplierSubmit(
                new SubmitSupplierChangeCommand(1L, ChangeType.BASIC_INFO, Map.of("name", "新名")), 99L, "联系人");

        ArgumentCaptor<Supplier> supplierCaptor = ArgumentCaptor.forClass(Supplier.class);
        verify(supplierRepository).save(supplierCaptor.capture());
        assertEquals("新名", supplierCaptor.getValue().getName());
        verify(changeRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("审核通过：将变更应用到供应商主表，并将变更置为已通过")
    void shouldApplyChangeToSupplierWhenApproved() {
        when(changeRequestRepository.findById(5L)).thenReturn(Optional.of(pendingBasicInfo(5L, 1L, "老名", "新名")));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier(1L, SupplierStatus.ACTIVE, "老名")));
        when(supplierRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(changeRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        handler.handleReview(new ReviewChangeCommand(5L, true, null), 7L, "审核员");

        ArgumentCaptor<Supplier> supplierCaptor = ArgumentCaptor.forClass(Supplier.class);
        verify(supplierRepository).save(supplierCaptor.capture());
        assertEquals("新名", supplierCaptor.getValue().getName());

        ArgumentCaptor<SupplierChangeRequest> reqCaptor = ArgumentCaptor.forClass(SupplierChangeRequest.class);
        verify(changeRequestRepository).save(reqCaptor.capture());
        assertEquals(ChangeRequestStatus.APPROVED, reqCaptor.getValue().getStatus());
        assertEquals(7L, reqCaptor.getValue().getReviewerId());
    }

    @Test
    @DisplayName("审核驳回：标记驳回并通知供应商主要联系人，不改动供应商主表")
    void shouldMarkRejectedAndNotifySupplierWhenRejected() {
        when(changeRequestRepository.findById(5L)).thenReturn(Optional.of(pendingBasicInfo(5L, 1L, "老名", "新名")));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier(1L, SupplierStatus.ACTIVE, "测试供应商")));
        when(contactRepository.findBySupplierId(1L)).thenReturn(List.of(SupplierContact.builder()
                .id(10L).supplierId(1L).name("联系人").phone("13800138000")
                .email("supp@test.com").isPrimary(true).build()));
        when(changeRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        handler.handleReview(new ReviewChangeCommand(5L, false, "信息不全"), 7L, "审核员");

        ArgumentCaptor<SupplierChangeRequest> reqCaptor = ArgumentCaptor.forClass(SupplierChangeRequest.class);
        verify(changeRequestRepository).save(reqCaptor.capture());
        assertEquals(ChangeRequestStatus.REJECTED, reqCaptor.getValue().getStatus());
        assertEquals("信息不全", reqCaptor.getValue().getReviewComment());
        verify(emailPort).sendChangeReviewResult("supp@test.com", "测试供应商", false, "信息不全");
        verify(supplierRepository, never()).save(any());
    }

    @Test
    @DisplayName("撤回待审核变更：标记为已撤回")
    void shouldWithdrawPendingChange() {
        when(changeRequestRepository.findPendingBySupplierId(1L))
                .thenReturn(List.of(pendingBasicInfo(5L, 1L, "老名", "新名")));
        when(changeRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        handler.handleWithdraw(new WithdrawChangeCommand(1L, ChangeType.BASIC_INFO));

        ArgumentCaptor<SupplierChangeRequest> reqCaptor = ArgumentCaptor.forClass(SupplierChangeRequest.class);
        verify(changeRequestRepository).save(reqCaptor.capture());
        assertEquals(ChangeRequestStatus.WITHDRAWN, reqCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("提交准入审核：待完善信息 → 待审核信息")
    void shouldTransitionToPendingReviewOnSubmitForReview() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier(1L, SupplierStatus.PENDING_INFO, "老名")));
        when(supplierRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        handler.handleSubmitForReview(new SubmitForReviewCommand(1L));

        ArgumentCaptor<Supplier> supplierCaptor = ArgumentCaptor.forClass(Supplier.class);
        verify(supplierRepository).save(supplierCaptor.capture());
        assertEquals(SupplierStatus.PENDING_REVIEW, supplierCaptor.getValue().getStatus());
    }
}
