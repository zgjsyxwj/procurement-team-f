package com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 证件到期提醒去重日志 JPA 实体，映射 supplier_cert_reminder_log 表（Req 12.3）。
 */
@Entity
@Table(name = "supplier_cert_reminder_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertReminderLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "certificate_id", nullable = false)
    private Long certificateId;

    @Column(name = "remind_node", nullable = false)
    private int remindNode;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
}
