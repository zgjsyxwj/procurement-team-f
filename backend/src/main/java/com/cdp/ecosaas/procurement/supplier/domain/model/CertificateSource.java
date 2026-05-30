package com.cdp.ecosaas.procurement.supplier.domain.model;

import lombok.Getter;

/**
 * 证件来源枚举（Req 10.9、10.10）。
 * <p>
 * {@code SUPPLIER_UPLOAD} 供应商上传（进入待审核）；{@code BUYER_MAINTAIN} 采购员维护（直接已通过）。
 */
@Getter
public enum CertificateSource {

    SUPPLIER_UPLOAD("供应商上传"),
    BUYER_MAINTAIN("采购员维护");

    private final String description;

    CertificateSource(String description) {
        this.description = description;
    }
}
