package com.cdp.ecosaas.procurement.auth.interfaces.rest;

import com.cdp.ecosaas.procurement.auth.application.command.*;
import com.cdp.ecosaas.procurement.auth.application.handler.AuthCommandHandler;
import com.cdp.ecosaas.procurement.auth.application.handler.PasswordCommandHandler;
import com.cdp.ecosaas.procurement.auth.infrastructure.security.AuthCookieWriter;
import com.cdp.ecosaas.procurement.auth.interfaces.dto.*;
import com.cdp.ecosaas.procurement.auth.shared.constants.AuthConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 内部用户认证 Controller
 * <p>
 * 提供内部用户手机号密码登录、SSO回调、登出、修改密码、忘记密码、重置密码等接口。
 * Cookie 写入统一委托 {@link AuthCookieWriter}，所有 Cookie 属性由
 * {@code auth.cookie.*} 配置控制。
 */
@RestController
@RequiredArgsConstructor
public class AuthController {

    private static final String ROLE_PREFIX = "ROLE_";
    private static final String ROLE_SUPPLIER = ROLE_PREFIX + "SUPPLIER";

    private final AuthCommandHandler authCommandHandler;
    private final PasswordCommandHandler passwordCommandHandler;
    private final AuthCookieWriter cookieWriter;

    /**
     * 内部用户手机号密码登录。
     * 成功后 Set-Cookie(JWT, HttpOnly) + Set-Cookie(XSRF-TOKEN)
     */
    @PostMapping("/api/internal/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest,
                                               HttpServletResponse httpResponse) {
        LoginCommand cmd = new LoginCommand(request.phone(), request.password(), AuthConstants.USER_TYPE_INTERNAL);
        String ipAddress = httpRequest.getRemoteAddr();

        LoginResult result = authCommandHandler.handleLogin(cmd, ipAddress);

        cookieWriter.setJwtCookie(httpResponse, result.token());
        cookieWriter.setCsrfCookie(httpResponse, UUID.randomUUID().toString());

        LoginResponse response = new LoginResponse(result.userId(), result.name(), result.role(), result.isFirstLogin());
        return ResponseEntity.ok(response);
    }

    /**
     * 处理 SAML Response 回调（SSO 登录）。
     */
    @PostMapping("/api/internal/auth/sso/callback")
    public ResponseEntity<LoginResponse> ssoCallback(@RequestParam("SAMLResponse") String samlResponse,
                                                     HttpServletRequest httpRequest,
                                                     HttpServletResponse httpResponse) {
        SsoLoginCommand cmd = new SsoLoginCommand(samlResponse);
        String ipAddress = httpRequest.getRemoteAddr();

        LoginResult result = authCommandHandler.handleSsoLogin(cmd, ipAddress);

        cookieWriter.setJwtCookie(httpResponse, result.token());
        cookieWriter.setCsrfCookie(httpResponse, UUID.randomUUID().toString());

        LoginResponse response = new LoginResponse(result.userId(), result.name(), result.role(), result.isFirstLogin());
        return ResponseEntity.ok(response);
    }

    /**
     * 登出：清除 JWT Cookie 和 CSRF Cookie（Max-Age=0）。
     */
    @PostMapping("/api/auth/logout")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest httpRequest,
                                                  HttpServletResponse httpResponse) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth != null ? (String) auth.getPrincipal() : null;
        String ipAddress = httpRequest.getRemoteAddr();

        if (userId != null) {
            LogoutCommand cmd = new LogoutCommand(Long.parseLong(userId), AuthConstants.USER_TYPE_INTERNAL);
            authCommandHandler.handleLogout(cmd, ipAddress);
        }

        SecurityContextHolder.clearContext();

        var session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        cookieWriter.clearJwtCookie(httpResponse);
        cookieWriter.clearCsrfCookie(httpResponse);

        return ResponseEntity.ok(new MessageResponse("登出成功"));
    }

    /**
     * 修改密码。
     */
    @PostMapping("/api/auth/change-password")
    public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                          HttpServletRequest httpRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) auth.getPrincipal();
        String userType = extractUserType(auth);
        String ipAddress = httpRequest.getRemoteAddr();

        ChangePasswordCommand cmd = new ChangePasswordCommand(
                Long.parseLong(userId), userType, request.oldPassword(), request.newPassword()
        );
        passwordCommandHandler.handleChangePassword(cmd, ipAddress);

        return ResponseEntity.ok(new MessageResponse("密码修改成功"));
    }

    /**
     * 忘记密码：发送重置邮件。
     * 无论邮箱是否存在，都返回统一提示。
     */
    @PostMapping("/api/auth/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        ForgotPasswordCommand cmd = new ForgotPasswordCommand(request.email(), AuthConstants.USER_TYPE_INTERNAL);
        passwordCommandHandler.handleForgotPassword(cmd);

        return ResponseEntity.ok(new MessageResponse("如果该邮箱已注册，您将收到重置邮件"));
    }

    /**
     * 通过 Token 重置密码。
     */
    @PostMapping("/api/auth/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request,
                                                         HttpServletRequest httpRequest) {
        String ipAddress = httpRequest.getRemoteAddr();
        ResetPasswordCommand cmd = new ResetPasswordCommand(request.token(), request.newPassword());
        passwordCommandHandler.handleResetPassword(cmd, ipAddress);

        return ResponseEntity.ok(new MessageResponse("密码重置成功"));
    }

    private String extractUserType(Authentication auth) {
        if (auth != null && auth.getAuthorities() != null) {
            boolean isSupplier = auth.getAuthorities().stream()
                    .anyMatch(a -> ROLE_SUPPLIER.equals(a.getAuthority()));
            return isSupplier ? AuthConstants.USER_TYPE_SUPPLIER : AuthConstants.USER_TYPE_INTERNAL;
        }
        return AuthConstants.USER_TYPE_INTERNAL;
    }
}
