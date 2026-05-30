package com.cdp.ecosaas.procurement.supplier.domain.model;

import lombok.Getter;

/**
 * 证件类型状态枚举（Req 11.1、11.4）。
 * <p>
 * 停用后保留历史数据，仅新上传时不再展示该类型。
 */
@Getter
public enum CertificateTypeStatus {

    ACTIVE("启用"),
    DISABLED("停用");

    private final String description;

    CertificateTypeStatus(String description) {
        this.description = description;
    }
}
