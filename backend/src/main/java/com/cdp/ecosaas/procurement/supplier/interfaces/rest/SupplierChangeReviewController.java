package com.cdp.ecosaas.procurement.supplier.interfaces.rest;

import com.cdp.ecosaas.procurement.shared.util.SecurityUtils;
import com.cdp.ecosaas.procurement.supplier.application.command.ReviewCertificateCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.ReviewChangeCommand;
import com.cdp.ecosaas.procurement.supplier.application.handler.CertificateCommandHandler;
import com.cdp.ecosaas.procurement.supplier.application.handler.SupplierChangeCommandHandler;
import com.cdp.ecosaas.procurement.supplier.application.handler.SupplierChangeQueryHandler;
import com.cdp.ecosaas.procurement.supplier.interfaces.dto.ChangeRecordResponse;
import com.cdp.ecosaas.procurement.supplier.interfaces.dto.ReviewRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 变更与证件审核接口（采购端，BUYER/ADMIN）—— 待审核变更列表/详情/通过/驳回、证件审核通过/驳回。
 */
@RestController
@RequiredArgsConstructor
public class SupplierChangeReviewController {

    private final SupplierChangeCommandHandler changeCommandHandler;
    private final SupplierChangeQueryHandler changeQueryHandler;
    private final CertificateCommandHandler certificateCommandHandler;

    /** 待审核变更列表（按数据范围裁剪，Req 5.1）。 */
    @GetMapping("/api/supplier-changes")
    public List<ChangeRecordResponse> pendingChanges() {
        return changeQueryHandler.findPendingChanges(SecurityUtils.getCurrentUserRole(), SecurityUtils.getCurrentUserId())
                .stream().map(ChangeRecordResponse::from).toList();
    }

    /** 变更详情（前后对比，Req 5.2）。 */
    @GetMapping("/api/supplier-changes/{id}")
    public ChangeRecordResponse changeDetail(@PathVariable Long id) {
        return ChangeRecordResponse.from(changeQueryHandler.findDetail(id));
    }

    /** 变更审核通过（变更生效，Req 5.3）。 */
    @PostMapping("/api/supplier-changes/{id}/approve")
    public ResponseEntity<Void> approveChange(@PathVariable Long id) {
        changeCommandHandler.handleReview(new ReviewChangeCommand(id, true, null),
                SecurityUtils.getCurrentUserId(), operatorName());
        return ResponseEntity.noContent().build();
    }

    /** 变更审核驳回（原因 + 通知供应商，Req 5.4）。 */
    @PostMapping("/api/supplier-changes/{id}/reject")
    public ResponseEntity<Void> rejectChange(@PathVariable Long id, @RequestBody ReviewRequest request) {
        changeCommandHandler.handleReview(new ReviewChangeCommand(id, false, request.reason()),
                SecurityUtils.getCurrentUserId(), operatorName());
        return ResponseEntity.noContent().build();
    }

    /** 证件审核通过（Req 10.7）。 */
    @PostMapping("/api/supplier-certificates/{id}/approve")
    public ResponseEntity<Void> approveCertificate(@PathVariable Long id) {
        certificateCommandHandler.handleReview(new ReviewCertificateCommand(id, true, null), operatorName());
        return ResponseEntity.noContent().build();
    }

    /** 证件审核驳回（原因，Req 10.8）。 */
    @PostMapping("/api/supplier-certificates/{id}/reject")
    public ResponseEntity<Void> rejectCertificate(@PathVariable Long id, @RequestBody ReviewRequest request) {
        certificateCommandHandler.handleReview(new ReviewCertificateCommand(id, false, request.reason()), operatorName());
        return ResponseEntity.noContent().build();
    }

    private String operatorName() {
        return String.valueOf(SecurityUtils.getCurrentUserId());
    }
}
