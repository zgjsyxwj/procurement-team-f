package com.cdp.ecosaas.procurement.supplier.interfaces.rest;

import com.cdp.ecosaas.procurement.supplier.application.command.ChangeCertTypeStatusCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.SaveCertTypeCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.UpdateCertTypeFieldsCommand;
import com.cdp.ecosaas.procurement.supplier.application.handler.CertificateTypeCommandHandler;
import com.cdp.ecosaas.procurement.supplier.application.handler.CertificateTypeQueryHandler;
import com.cdp.ecosaas.procurement.supplier.interfaces.dto.CertTypeFieldDto;
import com.cdp.ecosaas.procurement.supplier.interfaces.dto.CertTypeFieldsRequest;
import com.cdp.ecosaas.procurement.supplier.interfaces.dto.CertTypeRequest;
import com.cdp.ecosaas.procurement.supplier.interfaces.dto.CertTypeResponse;
import com.cdp.ecosaas.procurement.supplier.interfaces.dto.CertTypeStatusRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 证件类型字典接口（管理端，ADMIN）—— 增删改查/停用、差异化字段维护（Req 11.1-11.5）。
 */
@RestController
@RequestMapping("/api/admin/cert-types")
@RequiredArgsConstructor
public class CertificateTypeController {

    private final CertificateTypeCommandHandler commandHandler;
    private final CertificateTypeQueryHandler queryHandler;

    /** 证件类型列表（Req 11.1）。 */
    @GetMapping
    public List<CertTypeResponse> list() {
        return queryHandler.findAll().stream().map(CertTypeResponse::from).toList();
    }

    /** 新增证件类型（名称唯一，Req 11.2、11.3）。 */
    @PostMapping
    public CertTypeResponse create(@RequestBody CertTypeRequest request) {
        return CertTypeResponse.from(commandHandler.handleSave(
                new SaveCertTypeCommand(null, request.name(), request.remark())));
    }

    /** 编辑证件类型（Req 11.1）。 */
    @PutMapping("/{id}")
    public CertTypeResponse update(@PathVariable Long id, @RequestBody CertTypeRequest request) {
        return CertTypeResponse.from(commandHandler.handleSave(
                new SaveCertTypeCommand(id, request.name(), request.remark())));
    }

    /** 启用/停用证件类型（Req 11.4）。 */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> changeStatus(@PathVariable Long id, @RequestBody CertTypeStatusRequest request) {
        commandHandler.handleChangeStatus(new ChangeCertTypeStatusCommand(id, request.active()));
        return ResponseEntity.noContent().build();
    }

    /** 维护差异化字段（整体替换，Req 11.5）。 */
    @PutMapping("/{id}/fields")
    public CertTypeResponse updateFields(@PathVariable Long id, @RequestBody CertTypeFieldsRequest request) {
        List<com.cdp.ecosaas.procurement.supplier.domain.model.CertTypeField> fields =
                request.fields() == null ? List.of() : request.fields().stream().map(CertTypeFieldDto::toDomain).toList();
        return CertTypeResponse.from(commandHandler.handleUpdateFields(new UpdateCertTypeFieldsCommand(id, fields)));
    }
}
