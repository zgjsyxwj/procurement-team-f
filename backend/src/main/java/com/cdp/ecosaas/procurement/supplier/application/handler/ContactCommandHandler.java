package com.cdp.ecosaas.procurement.supplier.application.handler;

import com.cdp.ecosaas.procurement.shared.exception.BusinessException;
import com.cdp.ecosaas.procurement.supplier.application.command.DeleteContactCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.SaveContactCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.SetPrimaryContactCommand;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierContact;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierContactRepository;
import com.cdp.ecosaas.procurement.supplier.domain.service.ContactDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 联系人命令处理器（任务 8.5）—— 新增/编辑、删除、设主要联系人。
 * <p>
 * 主要联系人约束（设主自动取消原主、不可删唯一主要联系人）由 {@link ContactDomainService} 承载（Req 9.4、9.5）；
 * 采购员/供应商编辑均即时生效，无需审批（Req 9.6、9.8）。
 * 联系人邀请（邮件 + 邀请日志，Req 9.7/9.10）及操作审计字段按决策暂不实现。
 */
@Service
@RequiredArgsConstructor
public class ContactCommandHandler {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?\\d{6,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final SupplierContactRepository contactRepository;
    private final ContactDomainService contactDomainService;

    /** 新增/编辑联系人，即时生效；设为主要联系人时自动取消原主要联系人（Req 9.1、9.4、9.6、9.8）。 */
    @Transactional
    public SupplierContact handleSave(SaveContactCommand cmd) {
        validateFormat(cmd.phone(), cmd.email());
        SupplierContact saved = contactRepository.save(
                cmd.contactId() == null ? newContact(cmd) : rebuildContact(cmd));
        if (cmd.primary()) {
            List<SupplierContact> contacts = contactRepository.findBySupplierId(cmd.supplierId());
            contactDomainService.setPrimary(contacts, saved.getId());
            contactRepository.saveAll(contacts);
        }
        return saved;
    }

    /** 设为主要联系人，自动取消原主要联系人标记（Req 9.4）。 */
    @Transactional
    public void handleSetPrimary(SetPrimaryContactCommand cmd) {
        List<SupplierContact> contacts = contactRepository.findBySupplierId(cmd.supplierId());
        contactDomainService.setPrimary(contacts, cmd.contactId());
        contactRepository.saveAll(contacts);
    }

    /** 删除联系人；不可删除唯一的主要联系人（Req 9.5）。 */
    @Transactional
    public void handleDelete(DeleteContactCommand cmd) {
        List<SupplierContact> contacts = contactRepository.findBySupplierId(cmd.supplierId());
        contactDomainService.ensureCanDelete(contacts, cmd.contactId());
        contactRepository.deleteById(cmd.contactId());
    }

    // ---------- 私有 ----------

    private SupplierContact newContact(SaveContactCommand cmd) {
        return SupplierContact.builder()
                .supplierId(cmd.supplierId())
                .name(cmd.name()).phone(cmd.phone()).email(cmd.email())
                .isPrimary(cmd.primary())
                .position(cmd.position()).department(cmd.department())
                .build();
    }

    private SupplierContact rebuildContact(SaveContactCommand cmd) {
        SupplierContact existing = contactRepository.findById(cmd.contactId())
                .orElseThrow(() -> new BusinessException("联系人不存在：" + cmd.contactId()));
        return SupplierContact.builder()
                .id(existing.getId())
                .supplierId(existing.getSupplierId())
                .name(cmd.name()).phone(cmd.phone()).email(cmd.email())
                .isPrimary(cmd.primary())
                .position(cmd.position()).department(cmd.department())
                .build();
    }

    private void validateFormat(String phone, String email) {
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new BusinessException("联系人手机号格式不正确");
        }
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessException("联系人邮箱格式不正确");
        }
    }
}
