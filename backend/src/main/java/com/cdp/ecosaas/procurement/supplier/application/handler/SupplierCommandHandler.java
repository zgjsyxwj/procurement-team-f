package com.cdp.ecosaas.procurement.supplier.application.handler;

import com.cdp.ecosaas.procurement.shared.exception.BusinessException;
import com.cdp.ecosaas.procurement.supplier.application.command.ChangeSupplierStatusCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.CreateSupplierCommand;
import com.cdp.ecosaas.procurement.supplier.application.result.DisableImpactResult;
import com.cdp.ecosaas.procurement.supplier.domain.model.Supplier;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierContact;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierInvitationLog;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierStatus;
import com.cdp.ecosaas.procurement.supplier.shared.exception.SupplierNotFoundException;
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

    /**
     * 调整供应商状态（Req 7.7-7.11）—— 直接设为「合作中」或「已停用」，无需额外审批。
     * <p>
     * 路由：目标=已停用 → 停用并同步停用账号；目标=合作中 → 已停用则重新启用（同步启用账号），
     * 待完善/待审核信息则手动设为合作中（不动账号）。非法目标或非法流转抛异常，账号联动失败延后至 17.2。
     */
    @Transactional
    public void handleChangeStatus(ChangeSupplierStatusCommand cmd, Long operatorId, String operatorName) {
        Supplier supplier = supplierRepository.findById(cmd.supplierId())
                .orElseThrow(() -> new SupplierNotFoundException(cmd.supplierId()));
        SupplierStatus before = supplier.getStatus();

        switch (cmd.targetStatus()) {
            case DISABLED -> lifecycleService.disable(supplier, accountPort);
            case ACTIVE -> {
                if (before == SupplierStatus.DISABLED) {
                    lifecycleService.enable(supplier, accountPort);
                } else {
                    lifecycleService.activate(supplier);
                }
            }
            default -> throw new BusinessException(
                    "不支持将供应商状态调整为[" + cmd.targetStatus().getDescription() + "]");
        }
        supplierRepository.save(supplier);

        // Req 7.10：记录操作人/时间/前后状态/备注——按决策落库延后，当前仅记日志。
        log.info("供应商状态调整 supplierId={} {}→{} operator={}({}) remark={}",
                supplier.getId(), before.getDescription(), supplier.getStatus().getDescription(),
                operatorName, operatorId, cmd.remark());
    }

    /**
     * 停用前受影响事项清单（Req 7.12）。
     * <p>
     * 依赖未完成 RFQ/合同/签署/履约——相关模块尚未实现，当前返回空清单桩（待 8.8 + 对应模块就绪后填充）。
     */
    public DisableImpactResult getDisableImpact(Long supplierId) {
        supplierRepository.findById(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException(supplierId));
        return DisableImpactResult.none();
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
