package com.cdp.ecosaas.procurement.supplier.application.result;

import com.cdp.ecosaas.procurement.supplier.domain.model.CertExpiryStatus;
import com.cdp.ecosaas.procurement.supplier.domain.model.Supplier;

/**
 * 供应商列表项（Req 8.1）—— 在供应商基础上附主要联系人与证件到期状态标注。
 *
 * @param supplier            供应商（含名称、统一社会信用代码、状态）
 * @param primaryContactName  主要联系人姓名
 * @param primaryContactPhone 主要联系人电话
 * @param certExpiryStatus    证件到期状态标注（取当前有效已通过证件中最严重者；无证件为正常，Req 8.4、12.5）
 */
public record SupplierListItem(Supplier supplier, String primaryContactName,
                               String primaryContactPhone, CertExpiryStatus certExpiryStatus) {
}
