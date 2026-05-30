package com.cdp.ecosaas.procurement.auth.application.service;

import com.cdp.ecosaas.procurement.auth.infrastructure.persistence.entity.AuditLogEntity;
import com.cdp.ecosaas.procurement.auth.infrastructure.persistence.repository.JpaAuditLogRepository;
import com.cdp.ecosaas.procurement.auth.shared.enums.AuditEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 审计日志记录服务。
 * <p>
 * 提供统一的审计日志记录 API，封装日志实体构建和持久化逻辑。
 * 各 Handler 通过此服务记录安全事件，确保日志格式一致。
 * <p>
 * 记录内容包括：事件类型、操作时间、操作人、目标账号、IP地址、操作结果。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final JpaAuditLogRepository auditLogRepository;

    /**
     * 记录审计日志。
     *
     * @param eventType      事件类型
     * @param operatorId     操作人ID（系统操作为 null）
     * @param operatorName   操作人姓名
     * @param targetUserId   目标账号ID
     * @param targetUserName 目标账号姓名
     * @param ipAddress      客户端IP地址
     * @param result         操作结果（SUCCESS/FAILURE）
     * @param detail         详细信息
     */
    public void record(AuditEventType eventType,
                       Long operatorId,
                       String operatorName,
                       Long targetUserId,
                       String targetUserName,
                       String ipAddress,
                       String result,
                       String detail) {
        AuditLogEntity auditLog = AuditLogEntity.builder()
                .eventType(eventType.name())
                .operatorId(operatorId)
                .operatorName(operatorName)
                .targetUserId(targetUserId)
                .targetUserName(targetUserName)
                .ipAddress(ipAddress)
                .result(result)
                .detail(detail)
                .createdAt(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);

        log.debug("审计日志已记录: type={}, operator={}, target={}, result={}",
                eventType, operatorId, targetUserId, result);
    }

    /**
     * 记录成功事件的便捷方法。
     *
     * @param eventType      事件类型
     * @param operatorId     操作人ID
     * @param operatorName   操作人姓名
     * @param targetUserId   目标账号ID
     * @param targetUserName 目标账号姓名
     * @param ipAddress      客户端IP地址
     * @param detail         详细信息
     */
    public void recordSuccess(AuditEventType eventType,
                              Long operatorId,
                              String operatorName,
                              Long targetUserId,
                              String targetUserName,
                              String ipAddress,
                              String detail) {
        record(eventType, operatorId, operatorName, targetUserId, targetUserName, ipAddress, "SUCCESS", detail);
    }

    /**
     * 记录失败事件的便捷方法。
     *
     * @param eventType      事件类型
     * @param operatorId     操作人ID
     * @param operatorName   操作人姓名
     * @param targetUserId   目标账号ID
     * @param targetUserName 目标账号姓名
     * @param ipAddress      客户端IP地址
     * @param detail         详细信息
     */
    public void recordFailure(AuditEventType eventType,
                              Long operatorId,
                              String operatorName,
                              Long targetUserId,
                              String targetUserName,
                              String ipAddress,
                              String detail) {
        record(eventType, operatorId, operatorName, targetUserId, targetUserName, ipAddress, "FAILURE", detail);
    }
}
