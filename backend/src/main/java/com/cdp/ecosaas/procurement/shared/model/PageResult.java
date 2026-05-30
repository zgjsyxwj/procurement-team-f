package com.cdp.ecosaas.procurement.shared.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 通用分页结果。
 * <p>
 * 所有模块的分页响应可使用此类。
 *
 * @param <T> 列表项类型
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    /**
     * 从 Spring Data Page 构建 PageResult。
     */
    public static <T> PageResult<T> of(org.springframework.data.domain.Page<T> springPage) {
        return PageResult.<T>builder()
                .content(springPage.getContent())
                .page(springPage.getNumber())
                .size(springPage.getSize())
                .totalElements(springPage.getTotalElements())
                .totalPages(springPage.getTotalPages())
                .build();
    }
}
