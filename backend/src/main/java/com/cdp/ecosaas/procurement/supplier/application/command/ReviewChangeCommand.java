package com.cdp.ecosaas.procurement.supplier.application.command;

/**
 * 采购员审核供应商变更命令（Req 5.3、5.4）。
 *
 * @param changeRequestId 变更请求 ID
 * @param approve         true=通过（应用变更）；false=驳回
 * @param comment         审核意见 / 驳回原因
 */
public record ReviewChangeCommand(Long changeRequestId, boolean approve, String comment) {
}
