package com.cdp.ecosaas.procurement.supplier.application.handler;

import com.cdp.ecosaas.procurement.shared.exception.BusinessException;
import com.cdp.ecosaas.procurement.supplier.application.command.ReviewChangeCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.SubmitForReviewCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.SubmitSupplierChangeCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.UpdateSupplierInfoCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.WithdrawChangeCommand;
import com.cdp.ecosaas.procurement.supplier.application.support.SupplierBasicInfoFields;
import com.cdp.ecosaas.procurement.supplier.domain.model.ChangeRequestStatus;
import com.cdp.ecosaas.procurement.supplier.domain.model.ChangeSource;
import com.cdp.ecosaas.procurement.supplier.domain.model.ChangeType;
import com.cdp.ecosaas.procurement.supplier.domain.model.Supplier;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierChangeField;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierChangeRequest;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierContact;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierStatus;
import com.cdp.ecosaas.procurement.supplier.domain.port.EmailPort;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierChangeRequestRepository;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierContactRepository;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierRepository;
import com.cdp.ecosaas.procurement.supplier.domain.service.ChangeReviewService;
import com.cdp.ecosaas.procurement.supplier.shared.exception.InvalidSupplierStatusException;
import com.cdp.ecosaas.procurement.supplier.shared.exception.SupplierNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 供应商信息变更命令处理器 —— 直接编辑 / 提交变更 / 撤回 / 提交审核 / 审核（任务 8.2，BASIC_INFO）。
 * <p>
 * 按 {@code status} 分流：采购员直接编辑即时生效并记一条已通过变更（Req 49）；供应商在
 * 「待完善信息」草稿阶段直接生效、在「合作中」落待审核变更（Req 3.3、4.2）；审核通过时
 * 按设计「重建并保存」把变更应用到供应商主表（Req 5.3），驳回时通知供应商（Req 5.4）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierChangeCommandHandler {

    private final SupplierRepository supplierRepository;
    private final SupplierContactRepository contactRepository;
    private final SupplierChangeRequestRepository changeRequestRepository;
    private final ChangeReviewService changeReviewService;
    private final EmailPort emailPort;

    /** 采购员直接编辑：即时生效并记录一条已通过(BUYER)变更（Req 49）。 */
    @Transactional
    public void handleBuyerEdit(UpdateSupplierInfoCommand cmd, Long operatorId, String operatorName) {
        Supplier supplier = loadSupplier(cmd.supplierId());
        List<SupplierChangeField> changes = diff(supplier, cmd.changedFields());
        if (changes.isEmpty()) {
            return;
        }
        supplierRepository.save(SupplierBasicInfoFields.applyChanges(supplier, changes));

        LocalDateTime now = LocalDateTime.now();
        changeRequestRepository.save(SupplierChangeRequest.builder()
                .supplierId(supplier.getId())
                .changeType(ChangeType.BASIC_INFO)
                .source(ChangeSource.BUYER)
                .status(ChangeRequestStatus.APPROVED)
                .submitterId(operatorId)
                .submitterName(operatorName)
                .submittedAt(now)
                .reviewerId(operatorId)
                .reviewerName(operatorName)
                .reviewedAt(now)
                .fields(changes)
                .build());
    }

    /** 供应商提交变更：待完善信息草稿直接生效；合作中落待审核变更（Req 3.3、4.2）。 */
    @Transactional
    public void handleSupplierSubmit(SubmitSupplierChangeCommand cmd, Long submitterId, String submitterName) {
        Supplier supplier = loadSupplier(cmd.supplierId());
        List<SupplierChangeField> changes = diff(supplier, cmd.changedFields());
        if (changes.isEmpty()) {
            return;
        }

        if (supplier.getStatus() == SupplierStatus.PENDING_INFO) {
            // 入驻草稿：直接生效，不落变更记录
            supplierRepository.save(SupplierBasicInfoFields.applyChanges(supplier, changes));
            return;
        }
        if (supplier.getStatus() == SupplierStatus.ACTIVE) {
            // 合作中：同类冲突校验后落待审核变更（Req 3.6）
            changeReviewService.ensureNoPendingConflict(
                    changeRequestRepository.findPendingBySupplierId(supplier.getId()), cmd.changeType());
            changeRequestRepository.save(SupplierChangeRequest.builder()
                    .supplierId(supplier.getId())
                    .changeType(cmd.changeType())
                    .source(ChangeSource.SUPPLIER)
                    .status(ChangeRequestStatus.PENDING_REVIEW)
                    .submitterId(submitterId)
                    .submitterName(submitterName)
                    .submittedAt(LocalDateTime.now())
                    .fields(changes)
                    .build());
            // TODO(8.x)：通知关联采购员（Req 3.4）——待补「采购员ID→邮箱」解析端口后实发，当前仅记录。
            log.info("供应商 {} 提交{}变更待审核，应通知关联采购员（邮箱解析端口待补）",
                    supplier.getId(), cmd.changeType().getDescription());
            return;
        }
        throw new InvalidSupplierStatusException(
                "当前状态[" + supplier.getStatus().getDescription() + "]不允许提交信息变更");
    }

    /** 撤回某供应商指定类型的待审核变更（Req 3.7）。 */
    @Transactional
    public void handleWithdraw(WithdrawChangeCommand cmd) {
        SupplierChangeRequest pending = changeRequestRepository.findPendingBySupplierId(cmd.supplierId()).stream()
                .filter(request -> request.getChangeType() == cmd.changeType())
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "没有可撤回的待审核" + cmd.changeType().getDescription() + "变更"));
        pending.withdraw();
        changeRequestRepository.save(pending);
    }

    /** 提交准入审核：待完善信息 → 待审核信息（Req 4.4）。 */
    @Transactional
    public void handleSubmitForReview(SubmitForReviewCommand cmd) {
        Supplier supplier = loadSupplier(cmd.supplierId());
        supplier.submitForReview();
        supplierRepository.save(supplier);
    }

    /** 审核通过（应用变更）或驳回（通知供应商）（Req 5.3、5.4）。 */
    @Transactional
    public void handleReview(ReviewChangeCommand cmd, Long reviewerId, String reviewerName) {
        SupplierChangeRequest request = changeRequestRepository.findById(cmd.changeRequestId())
                .orElseThrow(() -> new BusinessException("变更不存在：" + cmd.changeRequestId()));
        Supplier supplier = loadSupplier(request.getSupplierId());

        if (cmd.approve()) {
            request.approve(reviewerId, reviewerName);
            supplierRepository.save(SupplierBasicInfoFields.applyChanges(supplier, request.getFields()));
            changeRequestRepository.save(request);
        } else {
            request.reject(reviewerId, reviewerName, cmd.comment());
            changeRequestRepository.save(request);
            notifySupplierRejected(supplier, cmd.comment());
        }
    }

    // ---------- 私有 ----------

    private Supplier loadSupplier(Long supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException(supplierId));
    }

    /** 基于当前供应商快照与提交值计算变更明细（仅改动字段，忽略未登记 key）。 */
    private List<SupplierChangeField> diff(Supplier supplier, Map<String, String> changedFields) {
        Map<String, String> before = SupplierBasicInfoFields.snapshot(supplier);
        Map<String, String> after = new LinkedHashMap<>(before);
        changedFields.forEach((key, value) -> {
            if (SupplierBasicInfoFields.isKnownField(key)) {
                after.put(key, value);
            }
        });
        return changeReviewService.computeChangedFields(before, after, SupplierBasicInfoFields.labels());
    }

    private void notifySupplierRejected(Supplier supplier, String reason) {
        contactRepository.findBySupplierId(supplier.getId()).stream()
                .filter(SupplierContact::isPrimary)
                .findFirst()
                .map(SupplierContact::getEmail)
                .ifPresent(email -> emailPort.sendChangeReviewResult(email, supplier.getName(), false, reason));
    }
}
