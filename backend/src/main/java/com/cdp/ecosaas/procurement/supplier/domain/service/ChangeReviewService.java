package com.cdp.ecosaas.procurement.supplier.domain.service;

import com.cdp.ecosaas.procurement.supplier.domain.model.ChangeRequestStatus;
import com.cdp.ecosaas.procurement.supplier.domain.model.ChangeType;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierChangeField;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierChangeRequest;
import com.cdp.ecosaas.procurement.supplier.shared.exception.DuplicatePendingChangeException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 变更审核领域服务（Req 3.3、3.6、5.2）。
 * <p>
 * 负责字段级前后差异计算与同类待审核冲突校验。变更生效（应用到 supplier 主表）由应用层
 * 在审核通过时重建并保存供应商完成（任务 8.2），以保持聚合根封装、避免暴露通用 setter。
 * 无状态、不依赖 Spring。
 */
public class ChangeReviewService {

    /**
     * 计算字段级前后差异，仅返回发生变更的字段（Req 5.2、50.2）。
     *
     * @param before 变更前字段值（fieldKey -> value）
     * @param after  变更后字段值（fieldKey -> value）
     * @param labels 字段显示名（fieldKey -> label）
     */
    public List<SupplierChangeField> computeChangedFields(Map<String, String> before,
                                                          Map<String, String> after,
                                                          Map<String, String> labels) {
        Set<String> keys = new LinkedHashSet<>();
        keys.addAll(after.keySet());
        keys.addAll(before.keySet());

        List<SupplierChangeField> changes = new ArrayList<>();
        for (String key : keys) {
            String beforeValue = before.get(key);
            String afterValue = after.get(key);
            if (!Objects.equals(beforeValue, afterValue)) {
                changes.add(SupplierChangeField.builder()
                        .fieldKey(key)
                        .fieldLabel(labels.getOrDefault(key, key))
                        .beforeValue(beforeValue)
                        .afterValue(afterValue)
                        .build());
            }
        }
        return changes;
    }

    /**
     * 校验同一变更类型是否已存在待审核变更，存在则拒绝（Req 3.6）。
     *
     * @throws DuplicatePendingChangeException 已存在同类待审核变更
     */
    public void ensureNoPendingConflict(Collection<SupplierChangeRequest> existing, ChangeType changeType) {
        boolean conflict = existing.stream().anyMatch(request ->
                request.getStatus() == ChangeRequestStatus.PENDING_REVIEW
                        && request.getChangeType() == changeType);
        if (conflict) {
            throw new DuplicatePendingChangeException(
                    "当前有待审核的" + changeType.getDescription() + "变更，请等待采购员确认");
        }
    }
}
