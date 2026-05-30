package com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 供应商变更字段明细 JPA 实体，映射 supplier_change_field 表。
 * <p>
 * 作为变更申请聚合的子记录，由 {@code JpaSupplierChangeRequestRepository} 按 change_request_id 整体替换式持久化。
 */
@Entity
@Table(name = "supplier_change_field")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierChangeFieldEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "change_request_id", nullable = false)
    private Long changeRequestId;

    @Column(name = "field_key", nullable = false, length = 64)
    private String fieldKey;

    @Column(name = "field_label", nullable = false, length = 64)
    private String fieldLabel;

    @Column(name = "before_value", columnDefinition = "text")
    private String beforeValue;

    @Column(name = "after_value", columnDefinition = "text")
    private String afterValue;
}
