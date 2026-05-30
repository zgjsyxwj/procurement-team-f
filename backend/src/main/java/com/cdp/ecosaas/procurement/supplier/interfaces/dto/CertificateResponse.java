package com.cdp.ecosaas.procurement.supplier.interfaces.dto;

import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierCertificate;

import java.time.LocalDate;
import java.util.Map;

/**
 * 证件响应（Req 10.5）—— 证件类型、有效期、审核状态、来源、当前/历史版本、差异化字段值。
 */
public record CertificateResponse(Long id, Long certTypeId, String fileUrl, String fileName,
                                  LocalDate validFrom, LocalDate validTo, String auditStatus, String rejectReason,
                                  String source, boolean currentValid, Map<String, Object> extraFields) {

    public static CertificateResponse from(SupplierCertificate c) {
        return new CertificateResponse(c.getId(), c.getCertTypeId(), c.getFileUrl(), c.getFileName(),
                c.getValidFrom(), c.getValidTo(),
                c.getAuditStatus() == null ? null : c.getAuditStatus().name(), c.getRejectReason(),
                c.getSource() == null ? null : c.getSource().name(), c.isCurrentValid(), c.getExtraFields());
    }
}
