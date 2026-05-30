package com.cdp.ecosaas.procurement.shared.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理器（跨模块共享）。
 * <p>
 * 统一捕获所有模块中的业务异常和系统异常，返回标准化的错误响应（backend_spec §5.4）：
 * <pre>
 * {
 *   "code": "AUTH.1001",
 *   "message": "AUTHENTICATION_FAILED",
 *   "detail": "手机号或密码错误",
 *   "timestamp": "..."
 * }
 * </pre>
 * HTTP 状态码根据 {@link BusinessException#getMessageCode()} 解析。
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.cdp.ecosaas.procurement")
public class GlobalExceptionHandler {

    private static final String MSG_AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";
    private static final String MSG_USER_NOT_FOUND = "USER_NOT_FOUND";
    private static final String MSG_UNSUPPORTED_USER_TYPE = "UNSUPPORTED_USER_TYPE";
    private static final String MSG_INVALID_SAML_RESPONSE = "INVALID_SAML_RESPONSE";
    private static final String MSG_RESET_TOKEN_INVALID = "RESET_TOKEN_INVALID";
    private static final String MSG_RESET_TOKEN_EXPIRED = "RESET_TOKEN_EXPIRED";
    private static final String MSG_RESET_TOKEN_USED = "RESET_TOKEN_USED";
    private static final String MSG_ACCOUNT_LOCKED = "ACCOUNT_LOCKED";
    private static final String MSG_ACCOUNT_DISABLED = "ACCOUNT_DISABLED";
    private static final String MSG_FORBIDDEN = "FORBIDDEN";
    private static final String MSG_RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    private static final String MSG_PASSWORD_POLICY_VIOLATION = "PASSWORD_POLICY_VIOLATION";
    private static final String MSG_PASSWORD_HISTORY_VIOLATION = "PASSWORD_HISTORY_VIOLATION";
    private static final String MSG_PHONE_ALREADY_USED = "PHONE_ALREADY_USED";
    private static final String MSG_EMAIL_ALREADY_USED = "EMAIL_ALREADY_USED";
    private static final String MSG_SUPER_ADMIN_IMMUTABLE = "SUPER_ADMIN_IMMUTABLE";
    private static final String MSG_CSRF_TOKEN_MISMATCH = "CSRF_TOKEN_MISMATCH";

    // 供应商模块（模块02，任务 17.1）
    private static final String MSG_SUPPLIER_NOT_FOUND = "SUPPLIER_NOT_FOUND";
    private static final String MSG_INVALID_SUPPLIER_STATUS = "INVALID_SUPPLIER_STATUS";
    private static final String MSG_DUPLICATE_PENDING_CHANGE = "DUPLICATE_PENDING_CHANGE";
    private static final String MSG_PRIMARY_CONTACT_REQUIRED = "PRIMARY_CONTACT_REQUIRED";
    private static final String MSG_INVALID_CERTIFICATE_FILE = "INVALID_CERTIFICATE_FILE";

    private static final String CODE_BAD_REQUEST = "COMMON.1000";
    private static final String MSG_BAD_REQUEST = "BAD_REQUEST";
    private static final String CODE_CONFLICT = "COMMON.1002";
    private static final String MSG_CONFLICT = "CONFLICT";
    private static final String CODE_INTERNAL_ERROR = "COMMON.2001";
    private static final String MSG_INTERNAL_ERROR = "INTERNAL_ERROR";

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException ex) {
        log.warn("业务异常: [{}] {} - {}", ex.getErrorCode(), ex.getMessageCode(), ex.getMessage());

        HttpStatus status = resolveHttpStatus(ex.getMessageCode());
        return ResponseEntity
                .status(status)
                .body(buildErrorResponse(ex.getErrorCode(), ex.getMessageCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", detail);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(CODE_BAD_REQUEST, MSG_BAD_REQUEST, detail));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("参数错误: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(CODE_BAD_REQUEST, MSG_BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(IllegalStateException ex) {
        log.warn("状态冲突: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(buildErrorResponse(CODE_CONFLICT, MSG_CONFLICT, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("系统内部错误", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse(CODE_INTERNAL_ERROR, MSG_INTERNAL_ERROR, "系统内部错误，请稍后重试"));
    }

    // ==================== 私有方法 ====================

    /**
     * 根据 messageCode 解析 HTTP 状态码。
     */
    private HttpStatus resolveHttpStatus(String messageCode) {
        if (messageCode == null) {
            return HttpStatus.BAD_REQUEST;
        }
        return switch (messageCode) {
            case MSG_AUTHENTICATION_FAILED, MSG_USER_NOT_FOUND, MSG_INVALID_SAML_RESPONSE,
                 MSG_RESET_TOKEN_INVALID, MSG_RESET_TOKEN_EXPIRED, MSG_RESET_TOKEN_USED,
                 MSG_CSRF_TOKEN_MISMATCH
                    -> HttpStatus.UNAUTHORIZED;
            case MSG_ACCOUNT_LOCKED -> HttpStatus.LOCKED;
            case MSG_ACCOUNT_DISABLED, MSG_FORBIDDEN -> HttpStatus.FORBIDDEN;
            case MSG_RESOURCE_NOT_FOUND, MSG_SUPPLIER_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case MSG_PASSWORD_POLICY_VIOLATION, MSG_PASSWORD_HISTORY_VIOLATION,
                 MSG_UNSUPPORTED_USER_TYPE, MSG_BAD_REQUEST, MSG_INVALID_CERTIFICATE_FILE
                    -> HttpStatus.BAD_REQUEST;
            case MSG_PHONE_ALREADY_USED, MSG_EMAIL_ALREADY_USED, MSG_SUPER_ADMIN_IMMUTABLE,
                 MSG_CONFLICT, MSG_INVALID_SUPPLIER_STATUS, MSG_DUPLICATE_PENDING_CHANGE,
                 MSG_PRIMARY_CONTACT_REQUIRED
                    -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
    }

    private Map<String, Object> buildErrorResponse(String code, String messageCode, String detail) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("message", messageCode);
        body.put("detail", detail);
        body.put("timestamp", LocalDateTime.now().toString());
        return body;
    }
}
