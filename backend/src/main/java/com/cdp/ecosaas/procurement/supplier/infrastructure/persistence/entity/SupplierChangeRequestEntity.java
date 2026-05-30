package com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 供应商信息变更申请/记录 JPA 实体，映射 supplier_change_request 表。
 */
@Entity
@Table(name = "supplier_change_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierChangeRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "change_type", nullable = false, length = 20)
    private String changeType;

    @Column(name = "source", nullable = false, length = 20)
    private String source;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "submitter_id", nullable = false)
    private Long submitterId;

    @Column(name = "submitter_name", nullable = false, length = 64)
    private String submitterName;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "reviewer_id")
    private Long reviewerId;

    @Column(name = "reviewer_name", length = 64)
    private String reviewerName;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_comment", length = 255)
    private String reviewComment;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    @Column(name = "reminded_at")
    private LocalDateTime remindedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
