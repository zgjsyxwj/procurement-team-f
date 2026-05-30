package com.cdp.ecosaas.procurement.supplier.interfaces.rest;

import com.cdp.ecosaas.procurement.shared.exception.BusinessException;
import com.cdp.ecosaas.procurement.shared.util.SecurityUtils;
import com.cdp.ecosaas.procurement.supplier.application.command.BuyerAddCertificateCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.UploadCertificateCommand;
import com.cdp.ecosaas.procurement.supplier.application.handler.CertificateCommandHandler;
import com.cdp.ecosaas.procurement.supplier.application.handler.SupplierQueryHandler;
import com.cdp.ecosaas.procurement.supplier.application.service.SupplierIdentityResolver;
import com.cdp.ecosaas.procurement.supplier.interfaces.dto.CertificateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * 证件接口（双端）—— 供应商门户端 {@code /api/supplier/certificates}（上传/列表）与采购端
 * {@code /api/suppliers/{id}/certificates}（列表/手动添加）。
 * <p>
 * 文件经 {@code FileStoragePort} 存 OSS（真实腾讯云 COS 接入延后，上传端点在接入前会失败）；
 * 差异化字段（extraFields）随多部分表单提交的解析暂未实现，先传空。
 */
@RestController
@RequiredArgsConstructor
public class SupplierCertificateController {

    private final CertificateCommandHandler commandHandler;
    private final SupplierQueryHandler queryHandler;
    private final SupplierIdentityResolver identityResolver;

    // ---------- 供应商门户端 ----------

    @GetMapping("/api/supplier/certificates")
    public List<CertificateResponse> myCertificates() {
        return list(currentSupplierId());
    }

    /** 上传/更新证件 → 待审核（Req 10.1-10.4）。真实 COS 上传接入前此端点会失败。 */
    @PostMapping(value = "/api/supplier/certificates", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CertificateResponse upload(@RequestParam Long certTypeId,
                                      @RequestPart("file") MultipartFile file,
                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validFrom,
                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validTo) {
        return CertificateResponse.from(commandHandler.handleUpload(new UploadCertificateCommand(
                currentSupplierId(), certTypeId, inputStream(file), file.getSize(),
                file.getOriginalFilename(), file.getContentType(), validFrom, validTo, null),
                operatorName()));
    }

    // ---------- 采购端 ----------

    @GetMapping("/api/suppliers/{supplierId}/certificates")
    public List<CertificateResponse> certificates(@PathVariable Long supplierId) {
        return list(supplierId);
    }

    /** 采购员手动添加证件 → 直接已通过（Req 10.9、10.11）。真实 COS 上传接入前此端点会失败。 */
    @PostMapping(value = "/api/suppliers/{supplierId}/certificates", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CertificateResponse buyerAdd(@PathVariable Long supplierId,
                                        @RequestParam Long certTypeId,
                                        @RequestPart("file") MultipartFile file,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validFrom,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validTo,
                                        @RequestParam(defaultValue = "true") boolean updateCurrentValid) {
        return CertificateResponse.from(commandHandler.handleBuyerAdd(new BuyerAddCertificateCommand(
                supplierId, certTypeId, inputStream(file), file.getSize(),
                file.getOriginalFilename(), file.getContentType(), validFrom, validTo, null, updateCurrentValid),
                operatorName()));
    }

    // ---------- 私有 ----------

    private List<CertificateResponse> list(Long supplierId) {
        return queryHandler.listCertificates(supplierId).stream().map(CertificateResponse::from).toList();
    }

    private java.io.InputStream inputStream(MultipartFile file) {
        try {
            return file.getInputStream();
        } catch (IOException e) {
            throw new BusinessException("读取证件文件失败：" + e.getMessage());
        }
    }

    private Long currentSupplierId() {
        return identityResolver.resolveSupplierId(SecurityUtils.getCurrentUserId());
    }

    private String operatorName() {
        return String.valueOf(SecurityUtils.getCurrentUserId());
    }
}
