package com.cdp.ecosaas.procurement.supplier.application.command;

/**
 * 采购员审核证件命令（Req 10.7、10.8）。
 *
 * @param certificateId 证件 ID
 * @param approve       true=通过；false=驳回
 * @param reason        驳回原因（可选）
 */
public record ReviewCertificateCommand(Long certificateId, boolean approve, String reason) {
}
