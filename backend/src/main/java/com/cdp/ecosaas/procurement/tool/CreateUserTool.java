package com.cdp.ecosaas.procurement.tool;

import com.cdp.ecosaas.procurement.auth.domain.model.UserRole;
import com.cdp.ecosaas.procurement.auth.domain.model.UserStatus;
import com.cdp.ecosaas.procurement.auth.domain.port.PasswordEncoderPort;
import com.cdp.ecosaas.procurement.auth.domain.service.PasswordDomainService;
import com.cdp.ecosaas.procurement.auth.infrastructure.persistence.entity.InternalUserEntity;
import com.cdp.ecosaas.procurement.auth.infrastructure.persistence.repository.InternalUserJpaDao;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

/**
 * 命令行工具：创建内部用户
 * <p>
 * 使用方式：以 Spring profile "create-user" 启动应用
 * <pre>
 *   java -jar app.jar --spring.profiles.active=create-user
 * </pre>
 * 启动后按提示输入 name、phone、email、password、role 即可创建用户。
 */
@Component
@Profile("create-user")
@RequiredArgsConstructor
public class CreateUserTool implements CommandLineRunner {

    private final InternalUserJpaDao internalUserJpaDao;
    private final PasswordDomainService passwordDomainService;
    private final PasswordEncoderPort passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== 创建内部用户工具 ===");
        System.out.println();

        // 1. 输入姓名
        System.out.print("姓名 (name): ");
        String name = scanner.nextLine().trim();
        if (name.isBlank()) {
            System.err.println("[错误] 姓名不能为空");
            return;
        }

        // 2. 输入手机号
        System.out.print("手机号 (phone): ");
        String phone = scanner.nextLine().trim();

        // 3. 输入邮箱
        System.out.print("邮箱 (email): ");
        String email = scanner.nextLine().trim();
        if (email.isBlank()) {
            System.err.println("[错误] 邮箱不能为空");
            return;
        }

        // 4. 输入密码
        System.out.print("密码 (password): ");
        String rawPassword = scanner.nextLine().trim();
        if (rawPassword.isBlank()) {
            System.err.println("[错误] 密码不能为空");
            return;
        }

        // 5. 输入角色
        System.out.println("可选角色: ADMIN, BUYER, BUSINESS_USER, SUPPLIER");
        System.out.print("角色 (role): ");
        String roleInput = scanner.nextLine().trim().toUpperCase();

        // --- 校验 ---
        // 校验角色
        UserRole role;
        try {
            role = UserRole.valueOf(roleInput);
        } catch (IllegalArgumentException e) {
            System.err.println("[错误] 无效角色: " + roleInput);
            return;
        }

        // 校验密码复杂度
        List<String> violations = passwordDomainService.validateComplexity(rawPassword);
        if (!violations.isEmpty()) {
            System.err.println("[错误] 密码不满足复杂度要求:");
            violations.forEach(v -> System.err.println("  - " + v));
            return;
        }

        // 校验手机号唯一
        if (!phone.isBlank() && internalUserJpaDao.findByPhone(phone).isPresent()) {
            System.err.println("[错误] 手机号已被使用: " + phone);
            return;
        }

        // 校验邮箱唯一
        if (internalUserJpaDao.findByEmail(email).isPresent()) {
            System.err.println("[错误] 邮箱已被使用: " + email);
            return;
        }

        // --- 创建用户 ---
        String passwordHash = passwordDomainService.hashPassword(rawPassword, passwordEncoder);
        LocalDateTime now = LocalDateTime.now();

        InternalUserEntity entity = InternalUserEntity.builder()
                .name(name)
                .phone(phone.isBlank() ? null : phone)
                .email(email)
                .passwordHash(passwordHash)
                .role(role.name())
                .status(UserStatus.ACTIVE.name())
                .superAdmin(false)
                .firstLogin(false)
                .failedAttempts(0)
                .createdAt(now)
                .updatedAt(now)
                .createdBy("CLI_TOOL")
                .updatedBy("CLI_TOOL")
                .build();

        InternalUserEntity saved = internalUserJpaDao.save(entity);

        System.out.println();
        System.out.println("✓ 用户创建成功!");
        System.out.println("  ID:    " + saved.getId());
        System.out.println("  姓名:  " + saved.getName());
        System.out.println("  邮箱:  " + saved.getEmail());
        System.out.println("  角色:  " + saved.getRole());
        System.out.println("  状态:  " + saved.getStatus());
    }
}
