package com.cdp.ecosaas.procurement.shared.exception;

import com.cdp.ecosaas.procurement.supplier.shared.exception.DuplicatePendingChangeException;
import com.cdp.ecosaas.procurement.supplier.shared.exception.InvalidCertificateFileException;
import com.cdp.ecosaas.procurement.supplier.shared.exception.InvalidSupplierStatusException;
import com.cdp.ecosaas.procurement.supplier.shared.exception.PrimaryContactRequiredException;
import com.cdp.ecosaas.procurement.supplier.shared.exception.SupplierNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * GlobalExceptionHandler 对供应商模块异常的 HTTP 状态映射（任务 17.1）。
 */
class GlobalExceptionHandlerSupplierStatusTests {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private HttpStatus statusOf(BusinessException ex) {
        ResponseEntity<Map<String, Object>> response = handler.handleBusinessException(ex);
        return HttpStatus.valueOf(response.getStatusCode().value());
    }

    @Test
    @DisplayName("供应商不存在 → 404")
    void shouldMapSupplierNotFoundToNotFound() {
        assertEquals(HttpStatus.NOT_FOUND, statusOf(new SupplierNotFoundException(1L)));
    }

    @Test
    @DisplayName("非法状态流转 → 409")
    void shouldMapInvalidStatusToConflict() {
        assertEquals(HttpStatus.CONFLICT, statusOf(new InvalidSupplierStatusException("非法流转")));
    }

    @Test
    @DisplayName("同类待审核变更冲突 → 409")
    void shouldMapDuplicatePendingChangeToConflict() {
        assertEquals(HttpStatus.CONFLICT, statusOf(new DuplicatePendingChangeException("已存在待审核变更")));
    }

    @Test
    @DisplayName("主要联系人约束 → 409")
    void shouldMapPrimaryContactRequiredToConflict() {
        assertEquals(HttpStatus.CONFLICT, statusOf(new PrimaryContactRequiredException("不能删除唯一主要联系人")));
    }

    @Test
    @DisplayName("证件文件非法 → 400")
    void shouldMapInvalidCertificateFileToBadRequest() {
        assertEquals(HttpStatus.BAD_REQUEST, statusOf(new InvalidCertificateFileException("格式不支持")));
    }
}
