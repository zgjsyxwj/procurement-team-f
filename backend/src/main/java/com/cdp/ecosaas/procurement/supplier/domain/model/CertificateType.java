package com.cdp.ecosaas.procurement.supplier.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 证件类型字典（Req 11）。
 * <p>
 * 停用后保留已使用该类型的历史证件数据，仅新上传时不再展示（Req 11.4）；名称唯一性由持久化层保证（Req 11.3）。
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CertificateType {

    private Long id;
    private String name;
    private CertificateTypeStatus status;
    private String remark;
    private List<CertTypeField> fields;

    /**
     * 停用证件类型（Req 11.4）。
     */
    public void disable() {
        this.status = CertificateTypeStatus.DISABLED;
    }

    /**
     * 启用证件类型。
     */
    public void enable() {
        this.status = CertificateTypeStatus.ACTIVE;
    }
}
