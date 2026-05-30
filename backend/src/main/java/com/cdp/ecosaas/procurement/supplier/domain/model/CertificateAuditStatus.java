package com.cdp.ecosaas.procurement.supplier.domain.model;

import lombok.Getter;

/**
 * 证件审核状态枚举（Req 10.4、10.7、10.8）。
 */
@Getter
public enum CertificateAuditStatus {

    PENDING_REVIEW("待审核"),
    APPROVED("已通过"),
    REJECTED("驳回");

    private final String description;

    CertificateAuditStatus(String description) {
        this.description = description;
    }
}
