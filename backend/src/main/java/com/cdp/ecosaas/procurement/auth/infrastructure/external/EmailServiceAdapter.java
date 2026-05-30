package com.cdp.ecosaas.procurement.auth.infrastructure.external;

import com.cdp.ecosaas.procurement.auth.domain.port.EmailPort;
import com.cdp.ecosaas.procurement.auth.infrastructure.config.AuthMailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * 邮件服务适配器 - {@link EmailPort} 的 SMTP 实现。
 * <p>
 * 通过 Spring Boot 自动装配的 {@link JavaMailSender} 发送 HTML 邮件，
 * SMTP 主机/端口/凭据由 {@code spring.mail.*} 提供，
 * 发件人地址、显示名、启用开关由 {@link AuthMailProperties} 提供。
 * <p>
 * 当 {@code auth.mail.enabled=false} 时仅记录日志，便于本地开发与测试。
 * 邮件正文使用内嵌简单 HTML，后续可替换为模板引擎（如 Thymeleaf）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailServiceAdapter implements EmailPort {

    private static final String SUBJECT_INITIAL_PASSWORD = "【CDP 采购平台】您的账号已创建";
    private static final String SUBJECT_RESET_LINK = "【CDP 采购平台】密码重置链接";
    private static final String SUBJECT_PASSWORD_CHANGED = "【CDP 采购平台】密码已变更";

    private final JavaMailSender javaMailSender;
    private final AuthMailProperties props;

    @Override
    public void sendInitialPassword(String email, String name, String password) {
        String body = buildInitialPasswordBody(name, password);
        send(email, name, SUBJECT_INITIAL_PASSWORD, body, "initial-password");
    }

    @Override
    public void sendPasswordResetLink(String email, String name, String resetLink) {
        String body = buildResetLinkBody(name, resetLink);
        send(email, name, SUBJECT_RESET_LINK, body, "password-reset-link");
    }

    @Override
    public void sendPasswordChangedNotification(String email, String name) {
        String body = buildPasswordChangedBody(name);
        send(email, name, SUBJECT_PASSWORD_CHANGED, body, "password-changed");
    }

    private void send(String toEmail, String toName, String subject, String htmlBody, String templateKey) {
        if (!props.enabled()) {
            log.info("邮件发送已禁用 (auth.mail.enabled=false)，跳过实际发送 - 模板: {}, 收件人: {}",
                    templateKey, toEmail);
            return;
        }

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setFrom(new InternetAddress(props.from(), props.fromName(), StandardCharsets.UTF_8.name()));
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            javaMailSender.send(message);
            log.info("邮件发送成功 - 模板: {}, 收件人: {}", templateKey, toEmail);
        } catch (MessagingException | UnsupportedEncodingException | MailException e) {
            log.error("邮件发送失败 - 模板: {}, 收件人: {}, 原因: {}",
                    templateKey, toEmail, e.getMessage(), e);
            // 上层不阻塞业务流程；如需强一致性可改为抛业务异常或入 Outbox 表重发。
        }
    }

    // ========== 邮件正文构造（占位简单 HTML，后续可替换为模板引擎） ==========

    private String buildInitialPasswordBody(String name, String password) {
        return "<p>" + escape(name) + " 您好：</p>"
                + "<p>您的 CDP 采购平台账号已创建，初始密码为：</p>"
                + "<p style='font-size:18px;font-weight:bold;'>" + escape(password) + "</p>"
                + "<p>为了账号安全，请登录后立即修改密码。</p>"
                + "<hr/><p style='color:#888;font-size:12px;'>本邮件由系统自动发送，请勿回复。</p>";
    }

    private String buildResetLinkBody(String name, String resetLink) {
        return "<p>" + escape(name) + " 您好：</p>"
                + "<p>您发起了密码重置请求，请点击下方链接完成重置（30 分钟内有效，仅可使用一次）：</p>"
                + "<p><a href='" + escape(resetLink) + "'>" + escape(resetLink) + "</a></p>"
                + "<p>若非本人操作，请忽略本邮件。</p>"
                + "<hr/><p style='color:#888;font-size:12px;'>本邮件由系统自动发送，请勿回复。</p>";
    }

    private String buildPasswordChangedBody(String name) {
        return "<p>" + escape(name) + " 您好：</p>"
                + "<p>您的 CDP 采购平台账号密码已成功变更。</p>"
                + "<p>若非本人操作，请尽快联系系统管理员。</p>"
                + "<hr/><p style='color:#888;font-size:12px;'>本邮件由系统自动发送，请勿回复。</p>";
    }

    private String escape(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
