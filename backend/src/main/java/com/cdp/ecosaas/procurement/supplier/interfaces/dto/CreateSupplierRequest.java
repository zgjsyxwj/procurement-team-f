package com.cdp.ecosaas.procurement.supplier.interfaces.dto;

import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierCategory;

/**
 * 创建供应商请求（Req 6.1-6.3）。
 *
 * @param name           企业名称
 * @param category       分类（DOMESTIC/OVERSEAS）
 * @param contactName    主要联系人姓名
 * @param contactPhone   主要联系人手机号（登录凭据）
 * @param contactEmail   主要联系人邮箱
 * @param sendInvitation true=保存并发送邀请；false=仅保存
 */
public record CreateSupplierRequest(String name, SupplierCategory category, String contactName,
                                    String contactPhone, String contactEmail, boolean sendInvitation) {
}
