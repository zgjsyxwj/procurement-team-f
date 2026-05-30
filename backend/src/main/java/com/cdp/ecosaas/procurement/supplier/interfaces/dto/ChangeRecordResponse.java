package com.cdp.ecosaas.procurement.supplier.interfaces.dto;

import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierChangeRequest;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 变更记录/待审核变更响应（前后对比，Req 5.2、50.2）。
 */
public record ChangeRecordResponse(Long id, Long supplierId, String changeType, String source, String status,
                                   Long submitterId, String submitterName, LocalDateTime submittedAt,
                                   Long reviewerId, String reviewerName, LocalDateTime reviewedAt,
                                   String reviewComment, List<ChangeFieldDto> fields) {

    public static ChangeRecordResponse from(SupplierChangeRequest r) {
        List<ChangeFieldDto> fields = r.getFields() == null ? List.of()
                : r.getFields().stream().map(ChangeFieldDto::from).toList();
        return new ChangeRecordResponse(
                r.getId(), r.getSupplierId(),
                r.getChangeType() == null ? null : r.getChangeType().name(),
                r.getSource() == null ? null : r.getSource().name(),
                r.getStatus() == null ? null : r.getStatus().name(),
                r.getSubmitterId(), r.getSubmitterName(), r.getSubmittedAt(),
                r.getReviewerId(), r.getReviewerName(), r.getReviewedAt(),
                r.getReviewComment(), fields);
    }
}
