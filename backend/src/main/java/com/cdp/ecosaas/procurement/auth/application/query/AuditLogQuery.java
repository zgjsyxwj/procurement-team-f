package com.cdp.ecosaas.procurement.auth.application.query;

import java.time.LocalDateTime;

/**
 * 审计日志查询参数。
 * <p>
 * 支持按事件类型、时间范围、目标用户ID筛选。
 *
 * @param eventType    事件类型筛选（可选）
 * @param startTime    开始时间（可选）
 * @param endTime      结束时间（可选）
 * @param targetUserId 目标用户ID筛选（可选）
 * @param page         页码（从0开始）
 * @param size         每页大小
 */
public record AuditLogQuery(String eventType, LocalDateTime startTime, LocalDateTime endTime,
                            Long targetUserId, int page, int size) {
}
