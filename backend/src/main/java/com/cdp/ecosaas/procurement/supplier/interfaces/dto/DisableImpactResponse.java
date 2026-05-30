package com.cdp.ecosaas.procurement.supplier.interfaces.dto;

import com.cdp.ecosaas.procurement.supplier.application.result.DisableImpactResult;

import java.util.List;

/**
 * 停用前受影响事项响应（Req 7.12）。
 */
public record DisableImpactResponse(boolean hasImpact, List<String> affectedItems) {

    public static DisableImpactResponse from(DisableImpactResult result) {
        return new DisableImpactResponse(result.hasImpact(), result.affectedItems());
    }
}
