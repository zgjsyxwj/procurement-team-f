package com.cdp.ecosaas.procurement.supplier.interfaces.rest;

import com.cdp.ecosaas.procurement.shared.model.PageResult;
import com.cdp.ecosaas.procurement.shared.util.SecurityUtils;
import com.cdp.ecosaas.procurement.supplier.application.command.ChangeSupplierStatusCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.CreateSupplierCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.UpdateSupplierInfoCommand;
import com.cdp.ecosaas.procurement.supplier.application.handler.SupplierChangeCommandHandler;
import com.cdp.ecosaas.procurement.supplier.application.handler.SupplierChangeQueryHandler;
import com.cdp.ecosaas.procurement.supplier.application.handler.SupplierCommandHandler;
import com.cdp.ecosaas.procurement.supplier.application.handler.SupplierQueryHandler;
import com.cdp.ecosaas.procurement.supplier.application.query.SupplierListQuery;
import com.cdp.ecosaas.procurement.supplier.application.result.SupplierListItem;
import com.cdp.ecosaas.procurement.supplier.domain.model.Supplier;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierStatus;
import com.cdp.ecosaas.procurement.supplier.interfaces.dto.ChangeRecordResponse;
import com.cdp.ecosaas.procurement.supplier.interfaces.dto.ChangeStatusRequest;
import com.cdp.ecosaas.procurement.supplier.interfaces.dto.CreateSupplierRequest;
import com.cdp.ecosaas.procurement.supplier.interfaces.dto.CreateSupplierResponse;
import com.cdp.ecosaas.procurement.supplier.interfaces.dto.DisableImpactResponse;
import com.cdp.ecosaas.procurement.supplier.interfaces.dto.SupplierDetailResponse;
import com.cdp.ecosaas.procurement.supplier.interfaces.dto.SupplierListResponse;
import com.cdp.ecosaas.procurement.supplier.interfaces.dto.UpdateSupplierInfoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 供应商管理接口（采购端，BUYER/ADMIN）—— 列表、创建、详情、直接编辑、停用影响、状态调整、变更记录。
 * <p>
 * 薄控制器：构建 Command/Query → 调用处理器 → 映射响应 DTO；当前用户由 {@link SecurityUtils} 提供。
 * 操作人姓名暂以用户 ID 字符串占位（JWT 无 name claim，显示名解析延后）。
 */
@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierCommandHandler commandHandler;
    private final SupplierChangeCommandHandler changeCommandHandler;
    private final SupplierQueryHandler queryHandler;
    private final SupplierChangeQueryHandler changeQueryHandler;

    /** 供应商列表（分页 / 名称模糊 / 状态筛选，按数据范围裁剪）。 */
    @GetMapping
    public PageResult<SupplierListResponse> list(@RequestParam(required = false) String nameKeyword,
                                                 @RequestParam(required = false) SupplierStatus status,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        PageResult<SupplierListItem> result = queryHandler.search(
                new SupplierListQuery(nameKeyword, status, page, size),
                SecurityUtils.getCurrentUserRole(), SecurityUtils.getCurrentUserId());
        return PageResult.<SupplierListResponse>builder()
                .content(result.getContent().stream().map(SupplierListResponse::from).toList())
                .page(result.getPage()).size(result.getSize())
                .totalElements(result.getTotalElements()).totalPages(result.getTotalPages())
                .build();
    }

    /** 创建供应商（仅保存 / 保存并发送邀请）。 */
    @PostMapping
    public ResponseEntity<CreateSupplierResponse> create(@RequestBody CreateSupplierRequest request) {
        CreateSupplierCommand cmd = new CreateSupplierCommand(request.name(), request.category(),
                request.contactName(), request.contactPhone(), request.contactEmail(), request.sendInvitation());
        Supplier saved = commandHandler.handleCreateSupplier(cmd, SecurityUtils.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(CreateSupplierResponse.from(saved));
    }

    /** 供应商详情。 */
    @GetMapping("/{id}")
    public SupplierDetailResponse detail(@PathVariable Long id) {
        return SupplierDetailResponse.from(queryHandler.getDetail(id));
    }

    /** 采购员直接编辑供应商信息（即时生效 + 记录变更，Req 49）。 */
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody UpdateSupplierInfoRequest request) {
        changeCommandHandler.handleBuyerEdit(new UpdateSupplierInfoCommand(id, request.changedFields()),
                SecurityUtils.getCurrentUserId(), operatorName());
        return ResponseEntity.noContent().build();
    }

    /** 停用前受影响事项清单（Req 7.12）。 */
    @GetMapping("/{id}/disable-impact")
    public DisableImpactResponse disableImpact(@PathVariable Long id) {
        return DisableImpactResponse.from(commandHandler.getDisableImpact(id));
    }

    /** 调整供应商状态（合作中/已停用，Req 7.7-7.11）。 */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> changeStatus(@PathVariable Long id, @RequestBody ChangeStatusRequest request) {
        commandHandler.handleChangeStatus(new ChangeSupplierStatusCommand(id, request.targetStatus(), request.remark()),
                SecurityUtils.getCurrentUserId(), operatorName());
        return ResponseEntity.noContent().build();
    }

    /** 变更记录（时间倒序，Req 50.2）。 */
    @GetMapping("/{id}/change-history")
    public List<ChangeRecordResponse> changeHistory(@PathVariable Long id) {
        return changeQueryHandler.findChangeHistory(id).stream().map(ChangeRecordResponse::from).toList();
    }

    private String operatorName() {
        return String.valueOf(SecurityUtils.getCurrentUserId());
    }
}
