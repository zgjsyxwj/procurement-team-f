package com.cdp.ecosaas.procurement.supplier.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 供应商信息变更申请/记录聚合根。
 * <p>
 * 统一承载：供应商提交的待审核变更（Req 3.3）、采购员直接编辑的即时生效记录
 * （{@code source=BUYER, status=APPROVED}，Req 49.3）、以及变更历史（Req 50）。
 * 审核通过/驳回/撤回的合法流转由本聚合根校验，非法流转抛 {@link IllegalStateException}。
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupplierChangeRequest {

    private Long id;
    private Long supplierId;
    private ChangeType changeType;
    private ChangeSource source;
    private ChangeRequestStatus status;
    private Long submitterId;
    private String submitterName;
    private LocalDateTime submittedAt;
    private Long reviewerId;
    private String reviewerName;
    private LocalDateTime reviewedAt;
    private String reviewComment;     // 审核意见/驳回原因
    private LocalDateTime withdrawnAt;
    private LocalDateTime remindedAt;  // 24h 未审核再提醒时间
    private List<SupplierChangeField> fields;

    /**
     * 审核通过（Req 5.3）。
     */
    public void approve(Long reviewerId, String reviewerName) {
        requirePending("审核通过");
        this.status = ChangeRequestStatus.APPROVED;
        this.reviewerId = reviewerId;
        this.reviewerName = reviewerName;
        this.reviewedAt = LocalDateTime.now();
    }

    /**
     * 审核驳回并记录原因（Req 5.4）。
     */
    public void reject(Long reviewerId, String reviewerName, String reason) {
        requirePending("审核驳回");
        this.status = ChangeRequestStatus.REJECTED;
        this.reviewerId = reviewerId;
        this.reviewerName = reviewerName;
        this.reviewComment = reason;
        this.reviewedAt = LocalDateTime.now();
    }

    /**
     * 供应商撤回待审核变更（Req 3.7）。
     */
    public void withdraw() {
        requirePending("撤回");
        this.status = ChangeRequestStatus.WITHDRAWN;
        this.withdrawnAt = LocalDateTime.now();
    }

    /**
     * 记录 24h 未审核再提醒时间（Req 5.8）。
     */
    public void markReminded() {
        this.remindedAt = LocalDateTime.now();
    }

    private void requirePending(String action) {
        if (this.status != ChangeRequestStatus.PENDING_REVIEW) {
            throw new IllegalStateException("仅待审核的变更可执行「" + action + "」，当前状态："
                    + (status == null ? "未知" : status.getDescription()));
        }
    }
}
