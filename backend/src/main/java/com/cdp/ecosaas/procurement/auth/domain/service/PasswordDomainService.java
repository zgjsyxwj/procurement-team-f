package com.cdp.ecosaas.procurement.auth.domain.service;

import com.cdp.ecosaas.procurement.auth.domain.model.PasswordHistory;
import com.cdp.ecosaas.procurement.auth.domain.port.PasswordEncoderPort;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 密码领域服务
 * <p>
 * 提供密码复杂度校验、密码历史检查、随机密码生成、密码哈希与验证等功能。
 * 这是一个无状态的领域服务，不依赖 Spring 框架（参见 backend_spec §3.1）。
 * <p>
 * 长度阈值通过构造器注入，由基础设施层从 {@code AuthPasswordProperties} 提供。
 */
public class PasswordDomainService {

    /**
     * 字符集（业务规则一部分，非环境配置；参见 backend_spec §12.3 允许例外）。
     */
    private static final String UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGIT_CHARS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
    private static final String ALL_CHARS = UPPERCASE_CHARS + LOWERCASE_CHARS + DIGIT_CHARS + SPECIAL_CHARS;

    private final int minPasswordLength;
    private final int generatedMinLength;
    private final int generatedMaxLength;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordDomainService(int minPasswordLength, int generatedMinLength, int generatedMaxLength) {
        if (minPasswordLength < 1) {
            throw new IllegalArgumentException("minPasswordLength must be >= 1");
        }
        if (generatedMinLength < minPasswordLength) {
            throw new IllegalArgumentException("generatedMinLength must be >= minPasswordLength");
        }
        if (generatedMaxLength < generatedMinLength) {
            throw new IllegalArgumentException("generatedMaxLength must be >= generatedMinLength");
        }
        this.minPasswordLength = minPasswordLength;
        this.generatedMinLength = generatedMinLength;
        this.generatedMaxLength = generatedMaxLength;
    }

    /**
     * 校验密码复杂度
     * <p>
     * 规则：至少 {@code minPasswordLength} 位，且同时包含大写字母、小写字母、数字和特殊字符。
     *
     * @param rawPassword 明文密码
     * @return 违规信息列表，空列表表示密码满足所有复杂度要求
     */
    public List<String> validateComplexity(String rawPassword) {
        List<String> violations = new ArrayList<>();

        if (rawPassword == null || rawPassword.length() < minPasswordLength) {
            violations.add("密码长度至少为 " + minPasswordLength + " 位");
        }

        if (rawPassword == null || !rawPassword.chars().anyMatch(Character::isUpperCase)) {
            violations.add("密码必须包含至少一个大写字母");
        }

        if (rawPassword == null || !rawPassword.chars().anyMatch(Character::isLowerCase)) {
            violations.add("密码必须包含至少一个小写字母");
        }

        if (rawPassword == null || !rawPassword.chars().anyMatch(Character::isDigit)) {
            violations.add("密码必须包含至少一个数字");
        }

        if (rawPassword == null || !containsSpecialChar(rawPassword)) {
            violations.add("密码必须包含至少一个特殊字符");
        }

        return violations;
    }

    /**
     * 检查密码历史
     */
    public boolean checkPasswordHistory(String newPassword, List<PasswordHistory> recentHistory,
                                        PasswordEncoderPort encoder) {
        if (newPassword == null || recentHistory == null || recentHistory.isEmpty()) {
            return false;
        }

        for (PasswordHistory history : recentHistory) {
            if (encoder.matches(newPassword, history.getPasswordHash())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生成满足所有复杂度要求的随机密码，长度在 [generatedMinLength, generatedMaxLength] 之间。
     */
    public String generateRandomPassword() {
        int length = generatedMinLength
                + secureRandom.nextInt(generatedMaxLength - generatedMinLength + 1);

        List<Character> passwordChars = new ArrayList<>();
        passwordChars.add(UPPERCASE_CHARS.charAt(secureRandom.nextInt(UPPERCASE_CHARS.length())));
        passwordChars.add(LOWERCASE_CHARS.charAt(secureRandom.nextInt(LOWERCASE_CHARS.length())));
        passwordChars.add(DIGIT_CHARS.charAt(secureRandom.nextInt(DIGIT_CHARS.length())));
        passwordChars.add(SPECIAL_CHARS.charAt(secureRandom.nextInt(SPECIAL_CHARS.length())));

        for (int i = 4; i < length; i++) {
            passwordChars.add(ALL_CHARS.charAt(secureRandom.nextInt(ALL_CHARS.length())));
        }

        Collections.shuffle(passwordChars, secureRandom);

        StringBuilder password = new StringBuilder(length);
        for (char c : passwordChars) {
            password.append(c);
        }
        return password.toString();
    }

    /**
     * 对明文密码进行哈希。
     */
    public String hashPassword(String rawPassword, PasswordEncoderPort encoder) {
        return encoder.encode(rawPassword);
    }

    /**
     * 验证明文密码与哈希是否匹配。
     */
    public boolean verifyPassword(String rawPassword, String hash, PasswordEncoderPort encoder) {
        return encoder.matches(rawPassword, hash);
    }

    private boolean containsSpecialChar(String str) {
        for (char c : str.toCharArray()) {
            if (SPECIAL_CHARS.indexOf(c) >= 0) {
                return true;
            }
        }
        return false;
    }
}
