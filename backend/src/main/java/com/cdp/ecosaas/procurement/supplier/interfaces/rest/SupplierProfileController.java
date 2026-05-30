package com.cdp.ecosaas.procurement.supplier.interfaces.rest;

import com.cdp.ecosaas.procurement.shared.util.SecurityUtils;
import com.cdp.ecosaas.procurement.supplier.application.command.SubmitForReviewCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.SubmitSupplierChangeCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.WithdrawChangeCommand;
import com.cdp.ecosaas.procurement.supplier.application.handler.CertificateTypeQueryHandler;
import com.cdp.ecosaas.procurement.supplier.application.handler.SupplierChangeCommandHandler;
import com.cdp.ecosaas.procurement.supplier.application.handler.SupplierChangeQueryHandler;
import com.cdp.ecosaas.procurement.supplier.application.handler.SupplierQueryHandler;
import com.cdp.ecosaas.procurement.supplier.application.service.SupplierIdentityResolver;
import com.cdp.ecosaas.procurement.supplier.domain.model.ChangeType;
import com.cdp.ecosaas.procurement.supplier.interfaces.dto.CertTypeResponse;
import com.cdp.ecosaas.procurement.supplier.interfaces.dto.ChangeRecordResponse;
import com.cdp.ecosaas.procurement.supplier.interfaces.dto.SupplierDetailResponse;
import com.cdp.ecosaas.procurement.supplier.interfaces.dto.UpdateSupplierInfoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 供应商门户端企业信息接口（SUPPLIER）—— 查看/编辑本企业信息、提交准入审核、待审核变更查询/撤回、可选证件类型。
 * <p>
 * 本企业 supplierId 由 {@link SupplierIdentityResolver} 从登录用户解析；编辑按状态分流
 * （待完善信息草稿直接生效 / 合作中落待审核变更）由 {@link SupplierChangeCommandHandler} 承载。
 * 当前仅 BASIC_INFO（银行多值变更延后）。
 */
@RestController
@RequiredArgsConstructor
public class SupplierProfileController {

    private final SupplierChangeCommandHandler changeCommandHandler;
    private final SupplierChangeQueryHandler changeQueryHandler;
    private final SupplierQueryHandler queryHandler;
    private final CertificateTypeQueryHandler certTypeQueryHandler;
    private final SupplierIdentityResolver identityResolver;

    /** 查看本企业信息（含银行信息，Req 3.1）。 */
    @GetMapping("/api/supplier/profile")
    public SupplierDetailResponse profile() {
        return SupplierDetailResponse.from(queryHandler.getDetail(currentSupplierId()));
    }

    /** 编辑企业信息（合作中→提交待审核；待完善信息→保存草稿，Req 3.3、4.2）。 */
    @PutMapping("/api/supplier/profile")
    public ResponseEntity<Void> update(@RequestBody UpdateSupplierInfoRequest request) {
        Long supplierId = currentSupplierId();
        changeCommandHandler.handleSupplierSubmit(
                new SubmitSupplierChangeCommand(supplierId, ChangeType.BASIC_INFO, request.changedFields()),
                SecurityUtils.getCurrentUserId(), operatorName());
        return ResponseEntity.noContent().build();
    }

    /** 提交准入审核（待完善信息→待审核信息，Req 4.4）。 */
    @PostMapping("/api/supplier/profile/submit-review")
    public ResponseEntity<Void> submitReview() {
        changeCommandHandler.handleSubmitForReview(new SubmitForReviewCommand(currentSupplierId()));
        return ResponseEntity.noContent().build();
    }

    /** 查询当前待审核变更（Req 3.6）。 */
    @GetMapping("/api/supplier/profile/pending-change")
    public List<ChangeRecordResponse> pendingChange() {
        return changeQueryHandler.findPendingBySupplier(currentSupplierId())
                .stream().map(ChangeRecordResponse::from).toList();
    }

    /** 撤回待审核变更（Req 3.7）。 */
    @PostMapping("/api/supplier/profile/pending-change/withdraw")
    public ResponseEntity<Void> withdraw() {
        changeCommandHandler.handleWithdraw(new WithdrawChangeCommand(currentSupplierId(), ChangeType.BASIC_INFO));
        return ResponseEntity.noContent().build();
    }

    /** 可选证件类型及其差异化字段（Req 11.6）。 */
    @GetMapping("/api/supplier/cert-types")
    public List<CertTypeResponse> certTypes() {
        return certTypeQueryHandler.findActive().stream().map(CertTypeResponse::from).toList();
    }

    private Long currentSupplierId() {
        return identityResolver.resolveSupplierId(SecurityUtils.getCurrentUserId());
    }

    private String operatorName() {
        return String.valueOf(SecurityUtils.getCurrentUserId());
    }
}
