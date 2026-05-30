package com.cdp.ecosaas.procurement.auth.interfaces.rest;

import com.cdp.ecosaas.procurement.auth.application.command.LoginCommand;
import com.cdp.ecosaas.procurement.auth.application.command.LoginResult;
import com.cdp.ecosaas.procurement.auth.application.handler.AuthCommandHandler;
import com.cdp.ecosaas.procurement.auth.infrastructure.security.AuthCookieWriter;
import com.cdp.ecosaas.procurement.auth.interfaces.dto.LoginRequest;
import com.cdp.ecosaas.procurement.auth.interfaces.dto.LoginResponse;
import com.cdp.ecosaas.procurement.auth.shared.constants.AuthConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 供应商认证 Controller
 * <p>
 * 提供供应商手机号密码登录接口，成功后通过 {@link AuthCookieWriter} 设置 JWT 与 CSRF Cookie。
 * 返回 isFirstLogin 标志供前端弹窗提示修改密码。
 */
@RestController
@RequestMapping("/api/supplier/auth")
@RequiredArgsConstructor
public class SupplierAuthController {

    private final AuthCommandHandler authCommandHandler;
    private final AuthCookieWriter cookieWriter;

    /**
     * 供应商手机号密码登录。
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest,
                                               HttpServletResponse httpResponse) {
        LoginCommand cmd = new LoginCommand(request.phone(), request.password(), AuthConstants.USER_TYPE_SUPPLIER);
        String ipAddress = httpRequest.getRemoteAddr();

        LoginResult result = authCommandHandler.handleLogin(cmd, ipAddress);

        cookieWriter.setJwtCookie(httpResponse, result.token());
        cookieWriter.setCsrfCookie(httpResponse, UUID.randomUUID().toString());

        LoginResponse response = new LoginResponse(result.userId(), result.name(), result.role(), result.isFirstLogin());
        return ResponseEntity.ok(response);
    }
}
