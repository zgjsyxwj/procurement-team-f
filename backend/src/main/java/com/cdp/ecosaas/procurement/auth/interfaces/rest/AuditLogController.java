package com.cdp.ecosaas.procurement.auth.interfaces.rest;

import com.cdp.ecosaas.procurement.auth.application.handler.AuditLogQueryHandler;
import com.cdp.ecosaas.procurement.auth.application.query.AuditLogQuery;
import com.cdp.ecosaas.procurement.auth.interfaces.dto.AuditLogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 审计日志 Controller
 * <p>
 * 提供审计日志分页查询接口，支持按事件类型、时间范围、目标账号筛选。
 * 仅 ADMIN 角色可访问。
 */
@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogQueryHandler auditLogQueryHandler;

    /**
     * 分页查询审计日志。
     * 支持按事件类型、时间范围、目标账号筛选。
     */
    @GetMapping
    public ResponseEntity<AuditLogResponse> listAuditLogs(
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) Long targetUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        AuditLogQuery query = new AuditLogQuery(eventType, startTime, endTime, targetUserId, page, size);
        AuditLogResponse response = auditLogQueryHandler.query(query);
        return ResponseEntity.ok(response);
    }
}
