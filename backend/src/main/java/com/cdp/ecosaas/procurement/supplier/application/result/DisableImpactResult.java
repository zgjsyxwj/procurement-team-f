package com.cdp.ecosaas.procurement.supplier.application.result;

import java.util.List;

/**
 * 停用前受影响事项清单（Req 7.12）。
 * <p>
 * 受影响事项来自未完成的 RFQ / 合同 / 签署 / 履约——这些模块（04/05/06 等）尚未实现，
 * 当前 {@link #none()} 返回空清单桩；待相关模块就绪后，由 8.8 查询层结合跨模块端口填充。
 *
 * @param hasImpact     是否存在受影响事项（用于前端风险提示）
 * @param affectedItems 受影响事项描述清单
 */
public record DisableImpactResult(boolean hasImpact, List<String> affectedItems) {

    /** 无受影响事项（占位，依赖模块未实现）。 */
    public static DisableImpactResult none() {
        return new DisableImpactResult(false, List.of());
    }
}
