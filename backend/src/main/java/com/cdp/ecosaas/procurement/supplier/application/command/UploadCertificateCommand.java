package com.cdp.ecosaas.procurement.supplier.application.command;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.Map;

/**
 * 供应商上传/更新证件命令（Req 10.1-10.4）—— 上传后置为「待审核」。
 * <p>
 * 文件经 {@code FileStoragePort} 存入 OSS（库内仅存访问标识）；上传同一证件类型会将其原当前有效版本置为历史。
 *
 * @param supplierId    所属供应商 ID
 * @param certTypeId    证件类型 ID
 * @param content       文件内容流
 * @param contentLength 文件字节数（白名单大小校验）
 * @param fileName      原始文件名
 * @param contentType   内容类型（白名单格式校验）
 * @param validFrom     有效期起始
 * @param validTo       有效期截止（须晚于起始）
 * @param extraFields   差异化字段提交值
 */
public record UploadCertificateCommand(Long supplierId, Long certTypeId, InputStream content, long contentLength,
                                       String fileName, String contentType, LocalDate validFrom, LocalDate validTo,
                                       Map<String, Object> extraFields) {
}
