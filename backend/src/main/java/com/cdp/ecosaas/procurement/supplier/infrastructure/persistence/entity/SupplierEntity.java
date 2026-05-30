package com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供应商企业 JPA 实体，映射 supplier 表。
 */
@Entity
@Table(name = "supplier")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supplier_code", nullable = false, length = 16)
    private String supplierCode;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "category", nullable = false, length = 16)
    private String category;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "unified_social_credit_code", length = 32)
    private String unifiedSocialCreditCode;

    @Column(name = "legal_person", length = 64)
    private String legalPerson;

    @Column(name = "registered_date")
    private LocalDate registeredDate;

    @Column(name = "registered_capital", precision = 18, scale = 2)
    private BigDecimal registeredCapital;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "general_taxpayer")
    private Boolean generalTaxpayer;

    @Column(name = "business_scope", columnDefinition = "text")
    private String businessScope;

    @Column(name = "enterprise_nature", length = 64)
    private String enterpriseNature;

    @Column(name = "sales_mode", length = 64)
    private String salesMode;

    @Column(name = "coverage_area", length = 255)
    private String coverageArea;

    @Column(name = "annual_revenue", precision = 18, scale = 2)
    private BigDecimal annualRevenue;

    @Column(name = "employee_count")
    private Integer employeeCount;

    @Column(name = "main_customers", length = 512)
    private String mainCustomers;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "updated_by", length = 64)
    private String updatedBy;

    @Version
    @Column(name = "version", nullable = false)
    private int version;

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
