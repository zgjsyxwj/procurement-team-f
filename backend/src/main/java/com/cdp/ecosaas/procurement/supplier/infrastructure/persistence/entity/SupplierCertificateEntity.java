package com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 供应商证件 JPA 实体，映射 supplier_certificate 表。
 * <p>
 * {@code extraFields} 映射 PostgreSQL JSONB 列（差异化字段提交值）。
 */
@Entity
@Table(name = "supplier_certificate")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierCertificateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "cert_type_id", nullable = false)
    private Long certTypeId;

    @Column(name = "file_url", nullable = false, length = 512)
    private String fileUrl;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to", nullable = false)
    private LocalDate validTo;

    @Column(name = "audit_status", nullable = false, length = 20)
    private String auditStatus;

    @Column(name = "reject_reason", length = 255)
    private String rejectReason;

    @Column(name = "source", nullable = false, length = 20)
    private String source;

    @Column(name = "is_current_valid", nullable = false)
    private boolean currentValid;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extra_fields", columnDefinition = "jsonb")
    private Map<String, Object> extraFields;

    @Column(name = "maintained_by", length = 64)
    private String maintainedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
