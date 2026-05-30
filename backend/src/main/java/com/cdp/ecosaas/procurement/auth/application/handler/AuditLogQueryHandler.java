package com.cdp.ecosaas.procurement.auth.application.handler;

import com.cdp.ecosaas.procurement.auth.application.query.AuditLogQuery;
import com.cdp.ecosaas.procurement.auth.infrastructure.persistence.entity.AuditLogEntity;
import com.cdp.ecosaas.procurement.auth.infrastructure.persistence.repository.JpaAuditLogRepository;
import com.cdp.ecosaas.procurement.auth.interfaces.dto.AuditLogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * 审计日志查询处理器。
 * <p>
 * 支持按事件类型、时间范围、目标用户ID的分页查询。
 * 返回 DTO 而非 JPA 实体。
 */
@Service
@RequiredArgsConstructor
public class AuditLogQueryHandler {

    private final JpaAuditLogRepository jpaAuditLogRepository;

    /**
     * 分页查询审计日志。
     *
     * @param query 查询参数
     * @return 分页审计日志响应 DTO
     */
    public AuditLogResponse query(AuditLogQuery query) {
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLogEntity> page = jpaAuditLogRepository.findByConditions(
                query.eventType(),
                query.startTime(),
                query.endTime(),
                query.targetUserId(),
                pageRequest
        );
        return toResponse(page);
    }

    private AuditLogResponse toResponse(Page<AuditLogEntity> page) {
        var items = page.getContent().stream()
                .map(entity -> new AuditLogResponse.AuditLogItem(
                        entity.getId(),
                        entity.getEventType(),
                        entity.getOperatorName(),
                        entity.getTargetUserName(),
                        entity.getIpAddress(),
                        entity.getResult(),
                        entity.getDetail(),
                        entity.getCreatedAt()
                ))
                .toList();

        return new AuditLogResponse(
                items,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
