package com.cdp.ecosaas.procurement.supplier.domain.model;

import lombok.Getter;

/**
 * 证件到期状态枚举（派生，不落库）。
 * <p>
 * 由 {@link SupplierCertificate#expiryStatus(java.time.LocalDate)} 根据有效期截止日与当天派生：
 * 「即将到期」定义为截止日在未来 30 天以内（Req 8.4、12.5、12.6）。
 */
@Getter
public enum CertExpiryStatus {

    NORMAL("正常"),
    EXPIRING_SOON("即将到期"),
    EXPIRED("已过期");

    private final String description;

    CertExpiryStatus(String description) {
        this.description = description;
    }
}
