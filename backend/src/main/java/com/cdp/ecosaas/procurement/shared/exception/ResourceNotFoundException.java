package com.cdp.ecosaas.procurement.shared.exception;

/**
 * 资源未找到异常。
 * <p>
 * 当请求的资源（用户、订单、合同等）不存在时抛出。
 * <p>
 * 错误码：{@code COMMON.1001} / {@code RESOURCE_NOT_FOUND}。
 */
public class ResourceNotFoundException extends BusinessException {

    public static final String CODE = "COMMON.1001";
    public static final String MESSAGE_CODE = "RESOURCE_NOT_FOUND";

    public ResourceNotFoundException(String detail) {
        super(CODE, MESSAGE_CODE, detail);
    }

    public ResourceNotFoundException(String resourceType, Long id) {
        super(CODE, MESSAGE_CODE, resourceType + " 不存在 (id=" + id + ")");
    }
}
