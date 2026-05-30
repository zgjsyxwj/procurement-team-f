package com.cdp.ecosaas.procurement.shared.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 通用分页查询基类。
 * <p>
 * 所有模块的分页查询参数可继承此类。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageQuery {

    /** 页码（从0开始） */
    private int page = 0;

    /** 每页大小 */
    private int size = 10;
}
