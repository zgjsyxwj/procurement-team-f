package com.cdp.ecosaas.procurement.supplier.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 供应商邀请邮件发送日志（领域模型，Req 6.8、9.10）。
 * <p>
 * 记录每次邀请邮件的收件人、发送时间与结果（SUCCESS/FAILURE）。纯领域对象，不含 JPA 注解。
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupplierInvitationLog {

    private Long id;
    private Long supplierId;
    private Long contactId;        // 收件联系人（可空）
    private String recipientEmail; // 收件邮箱
    private String sentBy;         // 发送人
    private LocalDateTime sentAt;  // 发送时间
    private String result;         // 发送结果：SUCCESS / FAILURE
}
