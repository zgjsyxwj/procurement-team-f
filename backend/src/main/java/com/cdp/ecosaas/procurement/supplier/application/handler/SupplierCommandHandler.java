package com.cdp.ecosaas.procurement.supplier.application.handler;

import com.cdp.ecosaas.procurement.shared.exception.BusinessException;
import com.cdp.ecosaas.procurement.supplier.application.command.CreateSupplierCommand;
import com.cdp.ecosaas.procurement.supplier.domain.model.Supplier;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierContact;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierInvitationLog;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierStatus;
import com.cdp.ecosaas.procurement.supplier.domain.port.BuyerSupplierRelationPort;
import com.cdp.ecosaas.procurement.supplier.domain.port.EmailPort;
import com.cdp.ecosaas.procurement.supplier.domain.port.SupplierAccountPort;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierContactRepository;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierInvitationLogRepository;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierRepository;
import com.cdp.ecosaas.procurement.supplier.domain.service.SupplierCodeGenerator;
import com.cdp.ecosaas.procurement.supplier.domain.service.SupplierLifecycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * 供应商命令处理器 —— 创建与邀请（任务 8.1）。
 * <p>
 * 创建流程（Req 6.1-6.6、6.8、7.2）：校验联系人格式 → 生成 VD 编号 → 保存供应商（创建成功）
 * 与主要联系人 → 调模块 01 建供应商账号（返回初始密码）→ 建立采购员-供应商管理关系（source=CREATED）
 * →（保存并邀请时）发邀请邮件、记邀请日志、发送成功则流转为「待进入」。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierCommandHandler {

    private static final String RELATION_SOURCE_CREATED = "CREATED";
    private static final String RESULT_SUCCESS = "SUCCESS";
    private static final String RESULT_FAILURE = "FAILURE";
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?\\d{6,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final SupplierRepository supplierRepository;
    private final SupplierContactRepository contactRepository;
    private final SupplierInvitationLogRepository invitationLogRepository;
    private final SupplierCodeGenerator codeGenerator;
    private final SupplierLifecycleService lifecycleService;
    private final SupplierAccountPort accountPort;
    private final BuyerSupplierRelationPort relationPort;
    private final EmailPort emailPort;

    /**
     * 创建供应商（可选「保存并发送邀请」）。
     *
     * @param operatorId 创建该供应商的采购员 ID（用于建立管理关系与邀请日志发送人）
     * @return 已保存的供应商（含生成的 ID 与编号）
     */
    @Transactional
    public Supplier handleCreateSupplier(CreateSupplierCommand cmd, Long operatorId) {
        validateContactFormat(cmd.contactPhone(), cmd.contactEmail());

        String supplierCode = codeGenerator.generate(supplierRepository.nextCodeSequence());
        Supplier saved = supplierRepository.save(Supplier.builder()
                .supplierCode(supplierCode)
                .name(cmd.name())
                .category(cmd.category())
                .status(SupplierStatus.CREATED)
                .build());

        SupplierContact savedContact = contactRepository.save(SupplierContact.builder()
                .supplierId(saved.getId())
                .name(cmd.contactName())
                .phone(cmd.contactPhone())
                .email(cmd.contactEmail())
                .isPrimary(true)
                .build());

        // 调模块 01 创建供应商登录账号（手机号为登录凭据），返回初始密码用于邀请邮件（Req 6.2）
        String initialPassword = accountPort.createAccount(
                saved.getId(), cmd.contactName(), cmd.contactPhone(), cmd.contactEmail());

        // 自动建立采购员-供应商管理关系（Req 6.4）
        relationPort.createRelation(operatorId, saved.getId(), RELATION_SOURCE_CREATED);

        if (cmd.sendInvitation()) {
            sendInvitation(saved, savedContact, initialPassword, operatorId);
        }
        return saved;
    }

    /**
     * 发送邀请邮件并记录日志；发送成功则流转「创建成功 → 待进入」（Req 6.3、6.7、6.8）。
     */
    private void sendInvitation(Supplier supplier, SupplierContact contact,
                                String initialPassword, Long operatorId) {
        String result;
        try {
            emailPort.sendSupplierInvitation(contact.getEmail(), contact.getName(),
                    supplier.getName(), contact.getPhone(), initialPassword);
            result = RESULT_SUCCESS;
        } catch (RuntimeException e) {
            result = RESULT_FAILURE;
            log.error("供应商邀请邮件发送失败 supplierId={}", supplier.getId(), e);
        }

        invitationLogRepository.save(SupplierInvitationLog.builder()
                .supplierId(supplier.getId())
                .contactId(contact.getId())
                .recipientEmail(contact.getEmail())
                .sentBy(operatorId == null ? null : String.valueOf(operatorId))
                .sentAt(LocalDateTime.now())
                .result(result)
                .build());

        if (RESULT_SUCCESS.equals(result)) {
            lifecycleService.invite(supplier);   // 创建成功 → 待进入
            supplierRepository.save(supplier);
        }
    }

    private void validateContactFormat(String phone, String email) {
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new BusinessException("主要联系人手机号格式不正确");
        }
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessException("主要联系人邮箱格式不正确");
        }
    }
}
