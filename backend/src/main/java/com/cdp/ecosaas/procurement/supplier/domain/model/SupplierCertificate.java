package com.cdp.ecosaas.procurement.supplier.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * 供应商证件实体。
 * <p>
 * 证件文件存于 OSS，库内仅存元数据与访问标识；审核状态独立于供应商整体准入状态。
 * 差异化字段提交值以 {@code extraFields} 承载（持久化为 JSONB）。
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupplierCertificate {

    /**
     * 「即将到期」阈值：截止日在未来该天数以内（含当天、含第 30 天）（Req 12.6）。
     */
    private static final int EXPIRING_SOON_DAYS = 30;

    private Long id;
    private Long supplierId;
    private Long certTypeId;
    private String fileUrl;                    // OSS 访问标识
    private String fileName;                   // 原始文件名
    private LocalDate validFrom;               // 有效期起始
    private LocalDate validTo;                 // 有效期截止
    private CertificateAuditStatus auditStatus;
    private String rejectReason;               // 驳回原因(可选)
    private CertificateSource source;
    private boolean isCurrentValid;            // 是否当前有效(否=历史版本)
    private Map<String, Object> extraFields;   // 差异化字段提交值
    private String maintainedBy;               // 维护人（Req 10.10）

    /**
     * 审核通过（Req 10.7）。
     */
    public void approve() {
        this.auditStatus = CertificateAuditStatus.APPROVED;
        this.rejectReason = null;
    }

    /**
     * 审核驳回并记录原因（Req 10.8）。
     */
    public void reject(String reason) {
        this.auditStatus = CertificateAuditStatus.REJECTED;
        this.rejectReason = reason;
    }

    /**
     * 标记为历史版本（非当前有效，Req 10.11）。
     */
    public void markHistorical() {
        this.isCurrentValid = false;
    }

    /**
     * 校验有效期截止日晚于起始日（Req 10.2）。
     *
     * @throws IllegalArgumentException 截止日不晚于起始日或日期缺失
     */
    public void validateValidityPeriod() {
        if (validFrom == null || validTo == null || !validTo.isAfter(validFrom)) {
            throw new IllegalArgumentException("证件有效期截止日期必须晚于起始日期");
        }
    }

    /**
     * 派生到期状态（Req 8.4、12.6）：
     * 截止日在今天之前为已过期；在未来 {@value #EXPIRING_SOON_DAYS} 天以内（含当天、含第 30 天）为即将到期；否则正常。
     *
     * @param today 当前日期（由调用方注入，便于测试）
     */
    public CertExpiryStatus expiryStatus(LocalDate today) {
        if (validTo.isBefore(today)) {
            return CertExpiryStatus.EXPIRED;
        }
        if (!validTo.isAfter(today.plusDays(EXPIRING_SOON_DAYS))) {
            return CertExpiryStatus.EXPIRING_SOON;
        }
        return CertExpiryStatus.NORMAL;
    }
}
