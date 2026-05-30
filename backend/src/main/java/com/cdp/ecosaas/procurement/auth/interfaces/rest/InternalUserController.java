package com.cdp.ecosaas.procurement.auth.interfaces.rest;

import com.cdp.ecosaas.procurement.auth.application.command.*;
import com.cdp.ecosaas.procurement.auth.application.handler.PasswordCommandHandler;
import com.cdp.ecosaas.procurement.auth.application.handler.UserCommandHandler;
import com.cdp.ecosaas.procurement.auth.application.handler.UserQueryHandler;
import com.cdp.ecosaas.procurement.auth.application.query.UserListQuery;
import com.cdp.ecosaas.procurement.auth.interfaces.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 内部用户管理 Controller
 * <p>
 * 提供用户列表查询、创建用户、修改角色、停用/启用、重置密码、手动解锁等管理接口。
 * 仅 ADMIN 角色可访问。
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserCommandHandler userCommandHandler;
    private final UserQueryHandler userQueryHandler;
    private final PasswordCommandHandler passwordCommandHandler;

    /**
     * 查询内部用户列表（分页、筛选、搜索）。
     */
    @GetMapping
    public ResponseEntity<UserListResponse> listUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        UserListQuery query = new UserListQuery(role, status, keyword, page, size);
        UserListResponse response = userQueryHandler.query(query);
        return ResponseEntity.ok(response);
    }

    /**
     * 创建内部用户。
     */
    @PostMapping
    public ResponseEntity<CreateUserResponse> createUser(@Valid @RequestBody CreateUserRequest request,
                                                         HttpServletRequest httpRequest) {
        Long operatorId = getOperatorId();
        String ipAddress = httpRequest.getRemoteAddr();

        CreateInternalUserCommand cmd = new CreateInternalUserCommand(
                request.name(), request.phone(), request.email(), request.role()
        );
        Long userId = userCommandHandler.handleCreateUser(cmd, operatorId, ipAddress);

        return ResponseEntity.ok(new CreateUserResponse(userId, "用户创建成功"));
    }

    /**
     * 修改用户角色。
     */
    @PatchMapping("/{id}/role")
    public ResponseEntity<MessageResponse> updateRole(@PathVariable Long id,
                                                      @Valid @RequestBody UpdateRoleRequest request,
                                                      HttpServletRequest httpRequest) {
        Long operatorId = getOperatorId();
        String ipAddress = httpRequest.getRemoteAddr();

        UpdateUserRoleCommand cmd = new UpdateUserRoleCommand(id, request.role());
        userCommandHandler.handleUpdateRole(cmd, operatorId, ipAddress);

        return ResponseEntity.ok(new MessageResponse("角色修改成功"));
    }

    /**
     * 停用/启用用户。
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<MessageResponse> updateStatus(@PathVariable Long id,
                                                        @Valid @RequestBody UpdateStatusRequest request,
                                                        HttpServletRequest httpRequest) {
        Long operatorId = getOperatorId();
        String ipAddress = httpRequest.getRemoteAddr();

        UpdateUserStatusCommand cmd = new UpdateUserStatusCommand(id, request.status());
        userCommandHandler.handleUpdateStatus(cmd, operatorId, ipAddress);

        return ResponseEntity.ok(new MessageResponse("状态修改成功"));
    }

    /**
     * 重置用户密码（管理员操作）。
     */
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@PathVariable Long id,
                                                         HttpServletRequest httpRequest) {
        Long operatorId = getOperatorId();
        String ipAddress = httpRequest.getRemoteAddr();

        AdminResetPasswordCommand cmd = new AdminResetPasswordCommand(id);
        passwordCommandHandler.handleAdminResetPassword(cmd, operatorId, ipAddress);

        return ResponseEntity.ok(new MessageResponse("密码已重置，新密码已发送至用户邮箱"));
    }

    /**
     * 手动解锁用户。
     */
    @PostMapping("/{id}/unlock")
    public ResponseEntity<MessageResponse> unlockUser(@PathVariable Long id,
                                                      HttpServletRequest httpRequest) {
        Long operatorId = getOperatorId();
        String ipAddress = httpRequest.getRemoteAddr();

        UnlockUserCommand cmd = new UnlockUserCommand(id);
        userCommandHandler.handleUnlock(cmd, operatorId, ipAddress);

        return ResponseEntity.ok(new MessageResponse("用户已解锁"));
    }

    // ==================== 私有方法 ====================

    private Long getOperatorId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) auth.getPrincipal();
        return Long.parseLong(userId);
    }
}
