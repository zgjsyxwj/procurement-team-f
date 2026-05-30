package com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 证件类型差异化字段 JPA 实体，映射 supplier_cert_type_field 表。
 * <p>
 * 作为证件类型聚合的子记录，由 {@code JpaCertificateTypeRepository} 按 cert_type_id 整体替换式持久化。
 */
@Entity
@Table(name = "supplier_cert_type_field")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertTypeFieldEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cert_type_id", nullable = false)
    private Long certTypeId;

    @Column(name = "field_key", nullable = false, length = 64)
    private String fieldKey;

    @Column(name = "field_label", nullable = false, length = 64)
    private String fieldLabel;

    @Column(name = "field_type", nullable = false, length = 32)
    private String fieldType;

    @Column(name = "required", nullable = false)
    private boolean required;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
