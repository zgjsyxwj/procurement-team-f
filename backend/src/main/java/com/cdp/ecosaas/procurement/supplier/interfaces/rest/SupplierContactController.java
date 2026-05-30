package com.cdp.ecosaas.procurement.supplier.interfaces.rest;

import com.cdp.ecosaas.procurement.shared.util.SecurityUtils;
import com.cdp.ecosaas.procurement.supplier.application.command.DeleteContactCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.SaveContactCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.SetPrimaryContactCommand;
import com.cdp.ecosaas.procurement.supplier.application.handler.ContactCommandHandler;
import com.cdp.ecosaas.procurement.supplier.application.handler.SupplierQueryHandler;
import com.cdp.ecosaas.procurement.supplier.application.service.SupplierIdentityResolver;
import com.cdp.ecosaas.procurement.supplier.interfaces.dto.ContactRequest;
import com.cdp.ecosaas.procurement.supplier.interfaces.dto.ContactResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 联系人接口（双端）—— 供应商门户端 {@code /api/supplier/contacts**}（本企业）与采购端
 * {@code /api/suppliers/{id}/contacts**}（按管理关系）。两端共用 {@link ContactCommandHandler}，
 * 主要联系人约束与即时生效一致；供应商端 supplierId 由登录用户解析。联系人邀请按决策暂不实现。
 */
@RestController
@RequiredArgsConstructor
public class SupplierContactController {

    private final ContactCommandHandler commandHandler;
    private final SupplierQueryHandler queryHandler;
    private final SupplierIdentityResolver identityResolver;

    // ---------- 供应商门户端 ----------

    @GetMapping("/api/supplier/contacts")
    public List<ContactResponse> myContacts() {
        return list(currentSupplierId());
    }

    @PostMapping("/api/supplier/contacts")
    public ContactResponse addMyContact(@RequestBody ContactRequest request) {
        return save(currentSupplierId(), null, request);
    }

    @PutMapping("/api/supplier/contacts/{id}")
    public ContactResponse editMyContact(@PathVariable Long id, @RequestBody ContactRequest request) {
        return save(currentSupplierId(), id, request);
    }

    @DeleteMapping("/api/supplier/contacts/{id}")
    public ResponseEntity<Void> deleteMyContact(@PathVariable Long id) {
        commandHandler.handleDelete(new DeleteContactCommand(currentSupplierId(), id));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/supplier/contacts/{id}/primary")
    public ResponseEntity<Void> setMyPrimary(@PathVariable Long id) {
        commandHandler.handleSetPrimary(new SetPrimaryContactCommand(currentSupplierId(), id));
        return ResponseEntity.noContent().build();
    }

    // ---------- 采购端（按供应商 ID） ----------

    @GetMapping("/api/suppliers/{supplierId}/contacts")
    public List<ContactResponse> contacts(@PathVariable Long supplierId) {
        return list(supplierId);
    }

    @PostMapping("/api/suppliers/{supplierId}/contacts")
    public ContactResponse addContact(@PathVariable Long supplierId, @RequestBody ContactRequest request) {
        return save(supplierId, null, request);
    }

    @PutMapping("/api/suppliers/{supplierId}/contacts/{contactId}")
    public ContactResponse editContact(@PathVariable Long supplierId, @PathVariable Long contactId,
                                       @RequestBody ContactRequest request) {
        return save(supplierId, contactId, request);
    }

    @DeleteMapping("/api/suppliers/{supplierId}/contacts/{contactId}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long supplierId, @PathVariable Long contactId) {
        commandHandler.handleDelete(new DeleteContactCommand(supplierId, contactId));
        return ResponseEntity.noContent().build();
    }

    // ---------- 私有 ----------

    private List<ContactResponse> list(Long supplierId) {
        return queryHandler.listContacts(supplierId).stream().map(ContactResponse::from).toList();
    }

    private ContactResponse save(Long supplierId, Long contactId, ContactRequest r) {
        return ContactResponse.from(commandHandler.handleSave(new SaveContactCommand(
                supplierId, contactId, r.name(), r.phone(), r.email(), r.primary(), r.position(), r.department())));
    }

    private Long currentSupplierId() {
        return identityResolver.resolveSupplierId(SecurityUtils.getCurrentUserId());
    }
}
