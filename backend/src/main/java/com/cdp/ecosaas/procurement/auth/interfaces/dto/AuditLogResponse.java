package com.cdp.ecosaas.procurement.auth.interfaces.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志分页响应 DTO
 *
 * @param content       审计日志列表
 * @param page          当前页码
 * @param size          每页大小
 * @param totalElements 总记录数
 * @param totalPages    总页数
 */
public record AuditLogResponse(
        List<AuditLogItem> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    /**
     * 审计日志列表项
     *
     * @param id             日志ID
     * @param eventType      事件类型
     * @param operatorName   操作人姓名
     * @param targetUserName 目标账号姓名
     * @param ipAddress      IP地址
     * @param result         操作结果
     * @param detail         详细信息
     * @param createdAt      创建时间
     */
    public record AuditLogItem(
            Long id,
            String eventType,
            String operatorName,
            String targetUserName,
            String ipAddress,
            String result,
            String detail,
            LocalDateTime createdAt
    ) {
    }
}
