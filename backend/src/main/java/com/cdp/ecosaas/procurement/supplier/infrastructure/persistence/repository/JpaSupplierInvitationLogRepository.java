package com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.repository;

import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierInvitationLog;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierInvitationLogRepository;
import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity.SupplierInvitationLogEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 供应商邀请日志仓储实现 —— 委托 {@link SupplierInvitationLogJpaDao}。
 * <p>
 * 邀请日志为追加写、字段简单且无领域行为，采用内联映射（不引入 MapStruct 映射器）。
 */
@Repository
@RequiredArgsConstructor
public class JpaSupplierInvitationLogRepository implements SupplierInvitationLogRepository {

    private final SupplierInvitationLogJpaDao jpaDao;

    @Override
    public SupplierInvitationLog save(SupplierInvitationLog invitationLog) {
        return toDomain(jpaDao.save(toEntity(invitationLog)));
    }

    private SupplierInvitationLogEntity toEntity(SupplierInvitationLog log) {
        return SupplierInvitationLogEntity.builder()
                .id(log.getId())
                .supplierId(log.getSupplierId())
                .contactId(log.getContactId())
                .recipientEmail(log.getRecipientEmail())
                .sentBy(log.getSentBy())
                .sentAt(log.getSentAt())
                .result(log.getResult())
                .build();
    }

    private SupplierInvitationLog toDomain(SupplierInvitationLogEntity entity) {
        return SupplierInvitationLog.builder()
                .id(entity.getId())
                .supplierId(entity.getSupplierId())
                .contactId(entity.getContactId())
                .recipientEmail(entity.getRecipientEmail())
                .sentBy(entity.getSentBy())
                .sentAt(entity.getSentAt())
                .result(entity.getResult())
                .build();
    }
}
