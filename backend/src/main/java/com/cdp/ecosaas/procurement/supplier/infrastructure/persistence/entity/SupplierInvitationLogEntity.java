package com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 供应商邀请邮件发送日志 JPA 实体，映射 supplier_invitation_log 表（Req 6.8、9.10）。
 */
@Entity
@Table(name = "supplier_invitation_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierInvitationLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "contact_id")
    private Long contactId;

    @Column(name = "recipient_email", nullable = false, length = 128)
    private String recipientEmail;

    @Column(name = "sent_by", length = 64)
    private String sentBy;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "result", nullable = false, length = 16)
    private String result;
}
