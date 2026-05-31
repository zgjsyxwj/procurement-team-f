package com.cdp.ecosaas.procurement.supplier.infrastructure.external;

import com.cdp.ecosaas.procurement.auth.domain.model.SupplierUser;
import com.cdp.ecosaas.procurement.auth.domain.model.UserStatus;
import com.cdp.ecosaas.procurement.auth.domain.port.PasswordEncoderPort;
import com.cdp.ecosaas.procurement.auth.domain.repository.SupplierUserRepository;
import com.cdp.ecosaas.procurement.auth.domain.service.PasswordDomainService;
import com.cdp.ecosaas.procurement.shared.exception.BusinessException;
import com.cdp.ecosaas.procurement.supplier.domain.port.SupplierAccountPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 供应商账号适配器 —— {@link SupplierAccountPort} 实现（任务 6.2a / 9.x / 17.2）。
 * <p>
 * 账号的创建/停用/启用回调模块 01 {@code auth_supplier_user}：创建时以主要联系人手机号为登录凭据、
 * 生成随机初始密码（哈希存储、明文返回供邀请邮件），停用/启用直接翻转账号状态。仅复用模块 01 已有
 * 的 {@link SupplierUserRepository}/{@link PasswordDomainService}/{@link PasswordEncoderPort}，不改模块 01 业务代码。
 * {@code findSupplierIdByUserId} 为只读解析，供应商门户端据此确定本企业数据范围。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SupplierAccountAdapter implements SupplierAccountPort {

    private final SupplierUserRepository supplierUserRepository;
    private final PasswordDomainService passwordDomainService;
    private final PasswordEncoderPort passwordEncoder;

    @Override
    public String createAccount(Long supplierId, String contactName, String phone, String email) {
        if (supplierUserRepository.findByPhone(phone).isPresent()) {
            throw new BusinessException("手机号 " + phone + " 已被占用，无法创建供应商账号");
        }

        String initialPassword = passwordDomainService.generateRandomPassword();
        supplierUserRepository.save(SupplierUser.builder()
                .name(contactName)
                .phone(phone)
                .email(email)
                .passwordHash(passwordEncoder.encode(initialPassword))
                .supplierId(supplierId)
                .status(UserStatus.ACTIVE)
                .isFirstLogin(true)
                .build());
        log.info("已开通供应商登录账号 supplierId={}, phone={}", supplierId, phone);
        return initialPassword;
    }

    @Override
    public Optional<Long> findSupplierIdByUserId(Long userId) {
        return supplierUserRepository.findById(userId).map(SupplierUser::getSupplierId);
    }

    @Override
    public void disableAccount(Long supplierId) {
        supplierUserRepository.findBySupplierId(supplierId).ifPresentOrElse(user -> {
            user.disable();
            supplierUserRepository.save(user);
            log.info("已停用供应商登录账号 supplierId={}", supplierId);
        }, () -> log.warn("停用跳过：供应商登录账号尚未开通 supplierId={}", supplierId));
    }

    @Override
    public void enableAccount(Long supplierId) {
        supplierUserRepository.findBySupplierId(supplierId).ifPresentOrElse(user -> {
            user.enable();
            supplierUserRepository.save(user);
            log.info("已启用供应商登录账号 supplierId={}", supplierId);
        }, () -> log.warn("启用跳过：供应商登录账号尚未开通 supplierId={}", supplierId));
    }
}
