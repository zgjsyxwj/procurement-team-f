package com.cdp.ecosaas.procurement.supplier.application.command;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.Map;

/**
 * 采购员手动添加证件命令（Req 10.9、10.11）—— 添加后直接「已通过」，不进入审核中心。
 *
 * @param updateCurrentValid true=更新当前有效证件（原版本置为历史）；false=新增一份历史证件
 */
public record BuyerAddCertificateCommand(Long supplierId, Long certTypeId, InputStream content, long contentLength,
                                         String fileName, String contentType, LocalDate validFrom, LocalDate validTo,
                                         Map<String, Object> extraFields, boolean updateCurrentValid) {
}
