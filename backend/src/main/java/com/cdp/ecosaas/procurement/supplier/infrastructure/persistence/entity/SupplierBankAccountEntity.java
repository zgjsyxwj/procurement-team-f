package com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 供应商银行账号 JPA 实体，映射 supplier_bank_account 表。
 * <p>
 * 作为供应商聚合的子记录，由 {@code JpaSupplierRepository} 按 supplier_id 整体替换式持久化。
 */
@Entity
@Table(name = "supplier_bank_account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierBankAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "account_name", nullable = false, length = 128)
    private String accountName;

    @Column(name = "bank_name", nullable = false, length = 128)
    private String bankName;

    @Column(name = "account_number", nullable = false, length = 64)
    private String accountNumber;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

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
