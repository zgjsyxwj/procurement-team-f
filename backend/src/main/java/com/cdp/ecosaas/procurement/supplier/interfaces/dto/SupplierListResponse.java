package com.cdp.ecosaas.procurement.supplier.interfaces.dto;

import com.cdp.ecosaas.procurement.supplier.application.result.SupplierListItem;
import com.cdp.ecosaas.procurement.supplier.domain.model.CertExpiryStatus;
import com.cdp.ecosaas.procurement.supplier.domain.model.Supplier;

/**
 * 供应商列表项响应（Req 8.1）—— 企业名称、统一社会信用代码、状态、主要联系人、电话、证件到期状态。
 */
public record SupplierListResponse(Long id, String supplierCode, String name, String category, String status,
                                   String unifiedSocialCreditCode, String primaryContactName,
                                   String primaryContactPhone, CertExpiryStatus certExpiryStatus) {

    public static SupplierListResponse from(SupplierListItem item) {
        Supplier s = item.supplier();
        return new SupplierListResponse(
                s.getId(), s.getSupplierCode(), s.getName(),
                s.getCategory() == null ? null : s.getCategory().name(),
                s.getStatus() == null ? null : s.getStatus().name(),
                s.getUnifiedSocialCreditCode(),
                item.primaryContactName(), item.primaryContactPhone(), item.certExpiryStatus());
    }
}
